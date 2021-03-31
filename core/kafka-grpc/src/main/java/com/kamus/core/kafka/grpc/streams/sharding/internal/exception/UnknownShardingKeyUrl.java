package com.kamus.core.kafka.grpc.streams.sharding.internal.exception;

public class UnknownShardingKeyUrl extends RuntimeException {

    public UnknownShardingKeyUrl() {
        super();
    }

    public UnknownShardingKeyUrl(String message) {
        super(message);
    }

}
