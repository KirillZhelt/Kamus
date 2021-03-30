package com.kamus.core.kafka.grpc.streams.sharding;

import com.google.common.base.Preconditions;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.util.Collections;
import java.util.List;

public class KafkaStreamsNameResolver extends NameResolver {

    private Listener2 listener;

    public KafkaStreamsNameResolver() {
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

    private void resolve() {
        Preconditions.checkNotNull(listener);

        ResolutionResult resolutionResult = ResolutionResult.newBuilder()
                                                    .build();
        listener.onResult(resolutionResult);
    }

    private List<EquivalentAddressGroup> discoverInstancesWithStores() {
        return Collections.emptyList();
    }

    @Override
    public String getServiceAuthority() {
        return "fakeAuthority";
    }

    @Override
    public void shutdown() {

    }

}
