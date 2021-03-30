package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import org.apache.kafka.streams.KafkaStreams;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

public class KafkaStreamsInternalNameResolver extends NameResolver {

    private final KafkaStreamsSingletonRegistry streamsRegistry;

    private Listener2 listener;

    public KafkaStreamsInternalNameResolver(KafkaStreamsSingletonRegistry streamsRegistry, String applicationId) {
        Preconditions.checkArgument(streamsRegistry.getApplicationId().equals(applicationId));

        this.streamsRegistry = streamsRegistry;
    }

    @Override
    public void start(Listener2 listener) {
        this.listener = listener;

        resolve();
    }

    @Override
    public void refresh() {
        resolve();
    }

    @Override
    public String getServiceAuthority() {
        return "fakeAuthority";
    }

    @Override
    public void shutdown() {

    }

    private void resolve() {
        try {
            KafkaStreams streams = streamsRegistry.getKafkaStreams();

            List<EquivalentAddressGroup> addresses = streams.allMetadata()
                                                             .stream()
                                                             .map(metadata -> new EquivalentAddressGroup(ImmutableList.of(new InetSocketAddress(metadata.host(), metadata.port()))))
                                                             .collect(Collectors.toList());

            listener.onResult(ResolutionResult.newBuilder().setAddresses(addresses).build());
        } catch (IllegalStateException ex) {
            listener.onError(Status.UNAVAILABLE.withCause(ex));
        }
    }

}
