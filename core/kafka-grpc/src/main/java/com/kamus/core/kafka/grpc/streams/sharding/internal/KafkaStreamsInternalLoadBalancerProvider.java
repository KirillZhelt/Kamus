package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;

import java.util.Map;

public class KafkaStreamsInternalLoadBalancerProvider extends LoadBalancerProvider {

    private final KafkaStreamsSingletonRegistry kafkaStreamsRegistry;
    private final Map<Class<? extends Message>, KafkaProtobufSerde<? extends Message>> serdesRegistry;

    public KafkaStreamsInternalLoadBalancerProvider(KafkaStreamsSingletonRegistry kafkaStreamsRegistry,
                                                    Map<Class<? extends Message>, KafkaProtobufSerde<? extends Message>> serdesRegistry) {
        Preconditions.checkNotNull(kafkaStreamsRegistry);
        Preconditions.checkNotNull(serdesRegistry);

        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.serdesRegistry = serdesRegistry;
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
        return new KafkaStreamsInternalLoadBalancer(helper, kafkaStreamsRegistry, serdesRegistry);
    }

}
