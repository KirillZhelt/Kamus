package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Message;
import com.kamus.common.grpcjava.Repository;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KafkaStreamsInternalPicker extends KafkaStreamsInternalLoadBalancer.Picker {

    private final KafkaStreamsSingletonRegistry kafkaStreamsRegistry;
    private final Map<Class<? extends Message>, KafkaProtobufSerde<? extends Message>> serdesRegistry;
    private final Map<HostInfo, LoadBalancer.Subchannel> subchannels;

    private static final Metadata.Key<Repository> SHARDING_KEY =
            Metadata.Key.of(
                    "SHARDING_KEY-bin",
                    ProtoUtils.metadataMarshaller(Repository.getDefaultInstance()));

    public KafkaStreamsInternalPicker(List<LoadBalancer.Subchannel> activeChannels,
                                      KafkaStreamsSingletonRegistry kafkaStreamsRegistry,
                                      Map<Class<? extends Message>, KafkaProtobufSerde<? extends Message>> serdesRegistry) {
        Preconditions.checkNotNull(activeChannels);
        Preconditions.checkArgument(!activeChannels.isEmpty());
        Preconditions.checkNotNull(kafkaStreamsRegistry);
        Preconditions.checkNotNull(serdesRegistry);

        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.serdesRegistry = serdesRegistry;

        ImmutableMap.Builder<HostInfo, LoadBalancer.Subchannel> subchannelsBuilder = ImmutableMap.builder();
        activeChannels.forEach(subchannel -> {
            toHostInfos(subchannel.getAddresses()).forEach(hostInfo -> subchannelsBuilder.put(hostInfo, subchannel));
        });
        this.subchannels = subchannelsBuilder.build();
    }

    @Override
    boolean isEquivalentTo(KafkaStreamsInternalLoadBalancer.Picker picker) {
        return false;
    }

    @Override
    public LoadBalancer.PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
        try {
            KafkaStreams kafkaStreams = kafkaStreamsRegistry.getKafkaStreams();

            Repository repository = args.getHeaders().get(SHARDING_KEY);

            KeyQueryMetadata metadata = kafkaStreams.queryMetadataForKey(
                    "CommitsCountPerRepositoryStore",
                    repository,
                    ((KafkaProtobufSerde<Repository>) serdesRegistry.get(repository.getClass())).serializer());

            return LoadBalancer.PickResult.withSubchannel(subchannels.get(metadata.getActiveHost()));
        } catch (IllegalStateException e) {
            return LoadBalancer.PickResult.withError(Status.UNAVAILABLE.withCause(e));
        }
    }

    private static Set<HostInfo> toHostInfos(EquivalentAddressGroup addressGroup) {
        return addressGroup.getAddresses()
                       .stream()
                       .map(socketAddr -> (InetSocketAddress) socketAddr)
                       .map(sockAddr -> new HostInfo(sockAddr.getHostName(), sockAddr.getPort()))
                       .collect(Collectors.toSet());
    }

}