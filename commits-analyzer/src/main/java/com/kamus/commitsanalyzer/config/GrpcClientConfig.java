package com.kamus.commitsanalyzer.config;

import com.google.protobuf.Message;
import com.kamus.core.kafka.grpc.streams.sharding.internal.KafkaStreamsInternalLoadBalancerProvider;
import com.kamus.core.kafka.grpc.streams.sharding.internal.KafkaStreamsInternalNameResolverProvider;
import com.kamus.core.kafka.grpc.streams.sharding.internal.KafkaStreamsSingletonRegistry;
import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import io.grpc.Channel;
import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Map;

@Configuration
public class GrpcClientConfig {

    @Autowired
    public void registerKafkaStreamsLoadBalancer(KafkaStreamsInternalLoadBalancerProvider loadBalancerProvider) {
        LoadBalancerRegistry.getDefaultRegistry().register(loadBalancerProvider);
    }

    @Autowired
    public void registerKafkaStreamsNameResolver(KafkaStreamsInternalNameResolverProvider nameResolverProvider) {
        NameResolverRegistry.getDefaultRegistry().register(nameResolverProvider);
    }

    @Bean
    public KafkaStreamsSingletonRegistry kafkaStreamsRegistry(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        return new KafkaStreamsSingletonRegistry(streamsBuilderFactoryBean);
    }

    @Bean
    public KafkaStreamsInternalNameResolverProvider kafkaStreamsNameResolverProvider(KafkaStreamsSingletonRegistry kafkaStreamsRegistry) {
        return new KafkaStreamsInternalNameResolverProvider(kafkaStreamsRegistry);
    }

    @Bean
    public KafkaStreamsInternalLoadBalancerProvider kafkaStreamsInternalLoadBalancerProvider(
            KafkaStreamsSingletonRegistry kafkaStreamsRegistry,
            Map<Class<? extends Message>, KafkaProtobufSerde<? extends Message>> serdesRegistry) {
        return new KafkaStreamsInternalLoadBalancerProvider(kafkaStreamsRegistry, serdesRegistry);
    }

    @Bean
    public Channel commitsAnalyzerChannel(String applicationId) {
        return ManagedChannelBuilder.forTarget(String.format("kstreaminternal:///%s", applicationId))
                       .defaultLoadBalancingPolicy("kstreaminternal")
                       .usePlaintext()
                       .build();
    }

    @Bean
    public CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceStub commitsAnalyzerClient(String applicationId) {
        return CommitsAnalyzerServiceGrpc.newStub(commitsAnalyzerChannel(applicationId));
    }

}
