package com.kamus.loader.updater.service;

import com.google.common.collect.ImmutableMap;
import com.kamus.core.model.Loader;
import com.kamus.core.zookeeper.Endpoints;
import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.loader.updater.service.exception.NoActiveLoaderException;
import com.kamus.loaderconfig.grpcjava.GetLoaderForBucketRequest;
import com.kamus.loaderconfig.grpcjava.GetLoaderForBucketResponse;
import com.kamus.loaderconfig.grpcjava.LoaderConfigurationServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class LoaderConfigurationService {

    private final LoaderConfigurationServiceGrpc.LoaderConfigurationServiceBlockingStub loaderConfigurationClient;

    public LoaderConfigurationService(LoaderConfigurationServiceGrpc.LoaderConfigurationServiceBlockingStub loaderConfigurationClient) {
        this.loaderConfigurationClient = loaderConfigurationClient;
    }

    public Loader getLoaderForBucket(int bucketId) {
        GetLoaderForBucketResponse response =
                loaderConfigurationClient.getLoaderForBucket(
                        GetLoaderForBucketRequest.newBuilder()
                                .setBucketId(bucketId)
                                .build());

        GetLoaderForBucketResponse.AssignedLoaderOneOfCase assignedLoaderOneOfCase = response.getAssignedLoaderOneOfCase();
        if (assignedLoaderOneOfCase == GetLoaderForBucketResponse.AssignedLoaderOneOfCase.LOADER) {
            return new Loader(
                    response.getLoader().getLoaderId(),
                    new Endpoints(ImmutableMap.of(DataLoaderServiceGrpc.SERVICE_NAME, response.getLoader().getLoaderEndpoint())));
        } else if (assignedLoaderOneOfCase == GetLoaderForBucketResponse.AssignedLoaderOneOfCase.NOACTIVELOADER) {
            throw new NoActiveLoaderException();
        }

        throw new IllegalStateException("Unreachable");
    }

}
