package com.kamus.loaderconfig.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@PropertySource("classpath:secrets.properties")
public class LoaderConfigConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public int bucketCount() {
        return 256;
    }

    @Bean
    public Executor loaderUpdatesExecutor() {
        return Executors.newFixedThreadPool(5);
    }

}
