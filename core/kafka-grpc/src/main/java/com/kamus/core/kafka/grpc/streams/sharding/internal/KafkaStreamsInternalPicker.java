package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kamus.loaderconfig.grpcjava.ShardingKey;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class KafkaStreamsInternalPicker extends KafkaStreamsInternalLoadBalancer.Picker {

    private final KafkaStreamsSingletonRegistry kafkaStreamsRegistry;
    private final ShardingKeysRegistry shardingKeysRegistry;
    private final Map<HostInfo, LoadBalancer.Subchannel> subchannels;

    private static final Metadata.Key<ShardingKey> SHARDING_KEY =
            Metadata.Key.of(
                    "SHARDING_KEY-bin",
                    ProtoUtils.metadataMarshaller(ShardingKey.getDefaultInstance()));

    public KafkaStreamsInternalPicker(List<LoadBalancer.Subchannel> activeChannels,
                                      KafkaStreamsSingletonRegistry kafkaStreamsRegistry,
                                      ShardingKeysRegistry shardingKeysRegistry) {
        Preconditions.checkNotNull(activeChannels);
        Preconditions.checkArgument(!activeChannels.isEmpty());
        Preconditions.checkNotNull(kafkaStreamsRegistry);
        Preconditions.checkNotNull(shardingKeysRegistry);

        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.shardingKeysRegistry = shardingKeysRegistry;

        ImmutableMap.Builder<HostInfo, LoadBalancer.Subchannel> subchannelsBuilder = ImmutableMap.builder();
        activeChannels.forEach(subchannel ->
                                       toHostInfos(subchannel.getAddresses()).forEach(hostInfo -> subchannelsBuilder.put(hostInfo, subchannel)));
        this.subchannels = subchannelsBuilder.build();
    }

    @Override
    boolean isEquivalentTo(KafkaStreamsInternalLoadBalancer.Picker picker) {
        return false;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public LoadBalancer.PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
        try {
            KafkaStreams kafkaStreams = kafkaStreamsRegistry.getKafkaStreams();

            ShardingKey key = args.getHeaders().get(SHARDING_KEY);
            if (Objects.isNull(key) || Objects.isNull(key.getKey())) {
                throw new IllegalArgumentException("SHARDING_KEY is not set.");
            }

            try {
                ShardingKeysRegistry.Entry e = shardingKeysRegistry.getEntry(key.getKey().getTypeUrl());

                KeyQueryMetadata metadata = kafkaStreams.queryMetadataForKey(
                        extractStoreName(args.getMethodDescriptor()),
                        key.getKey().unpack(e.messageType),
                        e.serializer);


                LoadBalancer.Subchannel subchannel = subchannels.get(metadata.getActiveHost());
                if (Objects.nonNull(subchannel)) {
                    return LoadBalancer.PickResult.withSubchannel(subchannel);
                } else {
                    return LoadBalancer.PickResult.withNoResult();
                }
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("Cannot deserialize sharding key", e);
            }
        } catch (IllegalStateException e) {
            return LoadBalancer.PickResult.withError(Status.UNAVAILABLE.withCause(e));
        }
    }

    private static String extractStoreName(MethodDescriptor<?, ?> methodDescriptor) {
        Preconditions.checkNotNull(methodDescriptor);
        Preconditions.checkNotNull(methodDescriptor.getBareMethodName());

        return StringUtils.capitalize(methodDescriptor.getBareMethodName());
    }

    private static Set<HostInfo> toHostInfos(EquivalentAddressGroup addressGroup) {
        return addressGroup.getAddresses()
                       .stream()
                       .map(socketAddr -> (InetSocketAddress) socketAddr)
                       .map(sockAddr -> new HostInfo(sockAddr.getAddress().getHostAddress(), sockAddr.getPort()))
                       .collect(Collectors.toSet());
    }

}