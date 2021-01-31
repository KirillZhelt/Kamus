package com.kamus.dataloader.config;

import com.kamus.core.spring.grpc.GrpcServerRunner;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class GrpcConfig {

    @Value("${grpc.data-loader-service.port}")
    private int dataLoaderServicePort;

    @Bean
    public Server grpcServer(Collection<BindableService> services) {
        ServerBuilder<?> builder = ServerBuilder.forPort(dataLoaderServicePort);
        services.forEach(builder::addService);
        return builder.build();
    }

    @Bean
    public GrpcServerRunner grpcServerRunner(Server grpcServer, ApplicationEventPublisher eventPublisher) {
        return new GrpcServerRunner(grpcServer, eventPublisher);
    }

}
