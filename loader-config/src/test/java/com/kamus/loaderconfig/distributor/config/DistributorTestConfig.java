package com.kamus.loaderconfig.distributor.config;

import com.kamus.loaderconfig.db.repository.DistributedBucketRepository;
import com.kamus.loaderconfig.distributor.BucketsDistributor;
import com.kamus.loaderconfig.service.BucketsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributorTestConfig {

    @Bean
    public BucketsService bucketsService(DistributedBucketRepository repository) {
        return new BucketsService(repository);
    }

    @Bean
    public BucketsDistributor distributor(BucketsService bucketsService) {
        return new BucketsDistributor(bucketsService, 256);
    }

}
