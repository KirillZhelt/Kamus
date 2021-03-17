package com.kamus.loader.updater.config;

import com.kamus.loaderconfig.grpcjava.LoaderConfigurationServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${loader.config.host:localhost}")
    private String loaderConfigServiceHost;

    @Value("${loader.config.port:1234}")
    private int loaderConfigServicePort;

    @Bean
    public Channel loaderConfigChannel() {
        return ManagedChannelBuilder.forAddress(loaderConfigServiceHost, loaderConfigServicePort)
                                  .usePlaintext()
                                  .build();
    }

    @Bean
    public LoaderConfigurationServiceGrpc.LoaderConfigurationServiceBlockingStub loaderConfigurationClient() {
        return LoaderConfigurationServiceGrpc.newBlockingStub(loaderConfigChannel());
    }

}
