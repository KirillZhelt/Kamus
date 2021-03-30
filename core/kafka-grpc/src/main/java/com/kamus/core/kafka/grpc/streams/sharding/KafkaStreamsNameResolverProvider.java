package com.kamus.core.kafka.grpc.streams.sharding;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

public class KafkaStreamsNameResolverProvider extends NameResolverProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsNameResolverProvider.class);

    private static final String SCHEME = "kstream";

    public KafkaStreamsNameResolverProvider() {
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (!SCHEME.equals(targetUri.getScheme())) {
            return null;
        }

        return new KafkaStreamsNameResolver();
    }

    private static Set<String> parseStoreNames(URI targetUri) {
        LOGGER.info("Configuring new KafkaStreamsNameResolver with target uri: {}", targetUri);

        String targetPath = Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
        Preconditions.checkArgument(targetPath.startsWith("/"),
                "the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
        String commaSeparatedStoreNames = targetPath.substring(1);

        return ImmutableSet.copyOf(commaSeparatedStoreNames.split(","));
    }

}
