package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;

public class KafkaStreamsInternalLoadBalancerProvider extends LoadBalancerProvider {

    private final KafkaStreamsSingletonRegistry kafkaStreamsRegistry;
    private final ShardingKeysRegistry shardingKeysRegistry;

    public KafkaStreamsInternalLoadBalancerProvider(KafkaStreamsSingletonRegistry kafkaStreamsRegistry,
                                                    ShardingKeysRegistry shardingKeysRegistry) {
        Preconditions.checkNotNull(kafkaStreamsRegistry);
        Preconditions.checkNotNull(shardingKeysRegistry);

        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.shardingKeysRegistry = shardingKeysRegistry;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String getPolicyName() {
        return "kstreaminternal";
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new KafkaStreamsInternalLoadBalancer(helper, kafkaStreamsRegistry, shardingKeysRegistry);
    }

}
