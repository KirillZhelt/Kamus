package com.kamus.dataloader.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.core.model.BucketId;
import com.kamus.dataloader.grpcjava.LoaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class LoaderConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(LoaderConfigurationService.class);

    private ConcurrentMap<BucketId, Set<Repository>> configuration;

    public LoaderConfigurationService() {
        this.configuration = new ConcurrentHashMap<>();
    }

    public Set<LoaderConfiguration> getCurrentConfiguration() {
        return configuration.entrySet()
                       .stream()
                       .map(e -> toLoaderConfiguration(e.getKey(), e.getValue()))
                       .collect(Collectors.toSet());
    }

    public int countCurrentConfigurationHash() {
        Repository[] allRepositories = configuration.values()
                                               .stream()
                                               .flatMap(Collection::stream)
                                               .toArray(Repository[]::new);
        return Objects.hash((Object[]) allRepositories);
    }

    public void setSources(List<LoaderConfiguration> assignedBuckets) {
        logger.info("Setting the configuration to {}", assignedBuckets);

        configuration = assignedBuckets.stream()
                                .collect(
                                        Collectors.toConcurrentMap(
                                                config -> new BucketId(config.getBucket()),
                                                config -> new HashSet<>(config.getRepositoryList())));
    }

    public void addRepository(int bucket, Repository repository) {
        Set<Repository> updatedRepositories = configuration.computeIfPresent(new BucketId(bucket), (id, repositories) -> {
            repositories.add(repository);
            return repositories;
        });

        if (Objects.isNull(updatedRepositories)) {
            logger.warn("Dropping repository assigned to the not owned bucket.");
        }

    }

    private LoaderConfiguration toLoaderConfiguration(BucketId bucketId, Set<Repository> repositories) {
        return LoaderConfiguration.newBuilder()
                       .setBucket(bucketId.getBucketId())
                       .addAllRepository(repositories)
                       .build();
    }

}
