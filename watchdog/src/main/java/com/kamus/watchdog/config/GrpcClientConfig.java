package com.kamus.watchdog.config;

import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    public Channel commitsAnalyzerChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 2000)
                       .usePlaintext()
                       .build();
    }

    @Bean
    public CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceBlockingStub commitsAnalyzerClient() {
        return CommitsAnalyzerServiceGrpc.newBlockingStub(commitsAnalyzerChannel());
    }

}
