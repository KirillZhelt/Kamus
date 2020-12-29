package com.kamus.dataloader.service;

import com.google.common.collect.ImmutableSet;
import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.grpcjava.LoaderConfiguration;
import org.apache.curator.x.async.api.AsyncCuratorFrameworkDsl;
import org.apache.curator.x.async.api.CreateOption;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LoaderConfigurationUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LoaderConfigurationUpdater.class);

    private static final String LOADERS_PATH = "/configuration/data-loader/";
    private static final String LOADER_MEMBER_PATH = LOADERS_PATH + "member_";

    private final AsyncCuratorFrameworkDsl zkClient;

    @Autowired
    public LoaderConfigurationUpdater(AsyncCuratorFrameworkDsl zkClient) {
        this.zkClient = zkClient;
    }

    @PostConstruct
    public void init() {
        announceToZookeeper();
    }

    private void announceToZookeeper() {
        logger.info("Announcing loader to Zookeeper.");
        zkClient.create()
                .withOptions(ImmutableSet.of(CreateOption.createParentsIfNeeded), CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(LOADER_MEMBER_PATH)
                .thenAccept(s -> logger.info(s))
                .exceptionally(t -> {
                    logger.error(t.getMessage(), t);
                    return null;
                });
    }

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
