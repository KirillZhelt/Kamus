package com.kamus.dataloader.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.grpcjava.LoaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoaderConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(LoaderConfigurationService.class);

    public LoaderConfiguration getCurrentConfiguration() {
        Repository repository = Repository.newBuilder()
                                        .setName("MayMayMay")
                                        .setOwner("KirillZhelt")
                                        .build();

        return LoaderConfiguration.newBuilder()
                       .addRepository(repository)
                       .build();
    }

}
