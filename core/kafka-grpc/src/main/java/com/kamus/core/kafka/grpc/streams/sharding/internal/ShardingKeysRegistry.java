package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import com.kamus.core.kafka.grpc.streams.sharding.internal.exception.UnknownShardingKeyUrl;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShardingKeysRegistry {

    private final Map<String, Entry<? extends Message>> messageForTypeUrl;

    public ShardingKeysRegistry() {
        this.messageForTypeUrl = new HashMap<>();

    }

    public <T extends Message> void registerShardingKey(Entry<T> entry) {
        Preconditions.checkNotNull(entry);
        messageForTypeUrl.put(entry.typeUrl, entry);
    }

    public Class<? extends Message> getMessageType(String typeUrl) {
        return Optional.ofNullable(messageForTypeUrl.get(typeUrl))
                       .map(e -> e.messageType)
                       .orElseThrow(() -> new UnknownShardingKeyUrl(typeUrl));
    }

    public Serializer<? extends Message> getSerializer(String typeUrl) {
        return Optional.ofNullable(messageForTypeUrl.get(typeUrl))
                       .map(e -> e.serializer)
                       .orElseThrow(() -> new UnknownShardingKeyUrl(typeUrl));
    }

    // registry guarantees that Entry has the corresponding serializer for the messageType, but returns a raw type
    @SuppressWarnings("rawtypes")
    public Entry getEntry(String typeUrl) {
        return messageForTypeUrl.get(typeUrl);
    }

    public static String buildTypeUrl(Class<? extends Message> message) {
        return "/" + message.getName();
    }

    public static class Entry<T extends Message> {

        public final String typeUrl;
        public final Class<T> messageType;
        public final Serializer<T> serializer;

        public Entry(Class<T> messageType, Serializer<T> serializer) {
            this(buildTypeUrl(messageType), messageType, serializer);
        }

        public Entry(String typeUrl,
                     Class<T> messageType, Serializer<T> serializer) {
            Preconditions.checkNotNull(typeUrl);
            Preconditions.checkNotNull(messageType);
            Preconditions.checkNotNull(serializer);

            this.typeUrl = typeUrl;
            this.messageType = messageType;
            this.serializer = serializer;
        }
    }

}
