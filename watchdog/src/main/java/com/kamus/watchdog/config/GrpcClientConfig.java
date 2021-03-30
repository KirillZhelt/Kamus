package com.kamus.watchdog.config;

import com.kamus.core.kafka.grpc.streams.sharding.KafkaStreamsNameResolverProvider;
import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Autowired
    public void registerKafkaStreamsNameResolver(KafkaStreamsNameResolverProvider nameResolverProvider) {
        NameResolverRegistry.getDefaultRegistry().register(nameResolverProvider);
    }

    @Bean
    public KafkaStreamsNameResolverProvider kafkaStreamsNameResolverProvider() {
        return new KafkaStreamsNameResolverProvider();
    }

    @Bean
    public Channel commitsAnalyzerChannel() {
        return ManagedChannelBuilder.forTarget("kstream:///CommitsCountPerRepositoryStore,store1,store2")
                       .usePlaintext()
                       .build();
    }

    @Bean
    public CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceBlockingStub commitsAnalyzerClient() {
        return CommitsAnalyzerServiceGrpc.newBlockingStub(commitsAnalyzerChannel());
    }

}
