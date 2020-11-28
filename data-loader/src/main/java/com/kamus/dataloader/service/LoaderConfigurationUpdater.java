package com.kamus.dataloader.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.loader.config.grpcjava.LoaderConfiguration;

public class LoaderConfigurationUpdater {

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
