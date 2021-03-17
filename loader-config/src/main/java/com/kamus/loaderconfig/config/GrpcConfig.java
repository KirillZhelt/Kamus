package com.kamus.loaderconfig.config;

import com.kamus.core.spring.grpc.GrpcServerRunner;
import com.kamus.loaderconfig.grpcjava.LoaderConfigurationServiceGrpc.LoaderConfigurationServiceImplBase;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Value("${grpc.loader-configuration-service.port}")
    private int loaderConfigurationServicePort;

    @Bean
    public Server grpcServer(LoaderConfigurationServiceImplBase loaderConfigurationService) {
        return ServerBuilder.forPort(loaderConfigurationServicePort)
                       .build();
    }

    @Bean
    public GrpcServerRunner grpcServerRunner(Server grpcServer) {
        return new GrpcServerRunner(grpcServer);
    }

}
