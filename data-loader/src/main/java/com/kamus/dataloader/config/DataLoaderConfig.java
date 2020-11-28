package com.kamus.dataloader.config;

import com.kamus.dataloader.service.LoaderConfigurationUpdater;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@PropertySource("classpath:secrets.properties")
public class DataLoaderConfig {

    @Bean
    public LoaderConfigurationUpdater loaderConfigurationUpdater() {
        return new LoaderConfigurationUpdater();
    }

}
