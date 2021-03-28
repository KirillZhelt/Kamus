package com.kamus.commitsanalyzer.config;

import com.kamus.core.spring.grpc.GrpcServerRunner;
import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Value("${grpc.commits-analyzer-service.port}")
    private int grpcServerPort;

    @Bean
    public Server grpcServer(CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceImplBase commitsAnalyzerService) {
        return ServerBuilder.forPort(grpcServerPort)
                       .addService(commitsAnalyzerService)
                       .build();
    }

    @Bean
    public GrpcServerRunner grpcServerRunner(Server grpcServer) {
        return new GrpcServerRunner(grpcServer);
    }

}
