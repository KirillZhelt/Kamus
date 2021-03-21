package com.kamus.loaderconfig.distributor;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.kamus.dataloader.grpcjava.*;
import com.kamus.loaderconfig.distributor.model.BucketsDistribution;
import com.kamus.loaderconfig.service.TrackedRepositoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class LoaderUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LoaderUpdater.class);

    private final Executor loaderUpdatesExecutor;
    private final TrackedRepositoriesService repositoriesService;

    public LoaderUpdater(Executor loaderUpdatesExecutor, TrackedRepositoriesService repositoriesService) {
        this.loaderUpdatesExecutor = loaderUpdatesExecutor;
        this.repositoriesService = repositoriesService;
    }

    public ListenableFuture<Boolean> updateLoader(DataLoaderServiceGrpc.DataLoaderServiceFutureStub stub, BucketsDistribution distribution) {
        ListenableFuture<GetCurrentDistributionHashResponse> currentDistributionHashFuture =
                 stub.getCurrentDistributionHash(GetCurrentDistributionHashRequest.newBuilder().build());

        ListenableFuture<Boolean> shouldUpdateDistributionFuture = Futures.transform(currentDistributionHashFuture,
                 currentDistributionHash -> shouldUpdateDistribution(currentDistributionHash, distribution),
                 loaderUpdatesExecutor);

        return Futures.transformAsync(shouldUpdateDistributionFuture,
                 shouldUpdateDistribution -> updateDistribution(shouldUpdateDistribution, stub, distribution),
                 loaderUpdatesExecutor);
    }

    private boolean shouldUpdateDistribution(GetCurrentDistributionHashResponse currentDistributionHash, BucketsDistribution distribution) {
//        return currentDistributionHash.getHash() != distribution.hashCode();
        return true;
    }

    private ListenableFuture<Boolean> updateDistribution(boolean shouldUpdateDistribution,
                                                         DataLoaderServiceGrpc.DataLoaderServiceFutureStub stub,
                                                         BucketsDistribution distribution) {
        if (!shouldUpdateDistribution) {
            logger.info("Loader's configuration is up-to-date.");

            return Futures.immediateFuture(false);
        }

        logger.info("Loader's configuration is outdated. Updating.");

        SetSourcesRequest.Builder requestBuilder = SetSourcesRequest.newBuilder();

        for (int bucketId : distribution.getAssignedBuckets()) {
            LoaderConfiguration.Builder configurationBuilder = LoaderConfiguration.newBuilder();
            configurationBuilder.setBucket(bucketId);
            configurationBuilder.addAllRepository(repositoriesService.getTrackedRepositoriesForBucket(bucketId));

            requestBuilder.addConfigurationBucket(configurationBuilder);
        }

        return Futures.transform(stub.setSources(requestBuilder.build()), r -> true, loaderUpdatesExecutor);
    }

}
