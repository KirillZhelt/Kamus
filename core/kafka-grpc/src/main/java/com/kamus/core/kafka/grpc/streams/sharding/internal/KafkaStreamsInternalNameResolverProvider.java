package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

public class KafkaStreamsInternalNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "kstreaminternal";

    private final KafkaStreamsSingletonRegistry streamsRegistry;

    public KafkaStreamsInternalNameResolverProvider(KafkaStreamsSingletonRegistry kafkaStreamsRegistry) {
        Preconditions.checkNotNull(kafkaStreamsRegistry);

        this.streamsRegistry = kafkaStreamsRegistry;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (!SCHEME.equals(targetUri.getScheme())) {
            return null;
        }

        String targetPath = Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
        Preconditions.checkArgument(targetPath.startsWith("/"),
                "the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
        String name = targetPath.substring(1);

        return new KafkaStreamsInternalNameResolver(streamsRegistry, name);
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }
}
