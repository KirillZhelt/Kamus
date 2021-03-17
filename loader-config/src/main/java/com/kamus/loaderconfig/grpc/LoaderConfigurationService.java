package com.kamus.loaderconfig.grpc;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.loaderconfig.distributor.DataLoadersCoordinator;
import com.kamus.core.model.BucketId;
import com.kamus.core.model.Loader;
import com.kamus.core.model.LoaderId;
import com.kamus.loaderconfig.grpcjava.*;
import com.kamus.loaderconfig.service.BucketsService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class LoaderConfigurationService extends LoaderConfigurationServiceGrpc.LoaderConfigurationServiceImplBase {

    private static final NoActiveLoader NO_ACTIVE_LOADER = NoActiveLoader.newBuilder().build();

    private final BucketsService bucketsService;
    private final DataLoadersCoordinator coordinator;
    private final int bucketCount;

    public LoaderConfigurationService(BucketsService bucketsService, DataLoadersCoordinator coordinator, int bucketCount) {
        this.bucketsService = bucketsService;
        this.coordinator = coordinator;
        this.bucketCount = bucketCount;
    }

    @Override
    public void getLoaderForBucket(GetLoaderForBucketRequest request, StreamObserver<GetLoaderForBucketResponse> responseObserver) {
        if (request.getBucketId() < 0 || request.getBucketId() >= bucketCount) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            return;
        }

        String loaderId = bucketsService.getLoaderForBucket(new BucketId(request.getBucketId())).getId();

        GetLoaderForBucketResponse response = coordinator.getLoaderById(new LoaderId(loaderId))
                                                      .map(this::buildLoaderForBucketResponse)
                                                      .orElseGet(this::buildNoActiveLoaderResponse);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private GetLoaderForBucketResponse buildLoaderForBucketResponse(Loader loader) {
        LoaderForBucket loaderForBucket = LoaderForBucket.newBuilder()
                                                  .setLoaderId(loader.getId().getId())
                                                  .setLoaderEndpoint(loader.getEndpoints().getEndpoints().get(DataLoaderServiceGrpc.SERVICE_NAME))
                                                  .build();

        return GetLoaderForBucketResponse.newBuilder().setLoader(loaderForBucket).build();
    }

    private GetLoaderForBucketResponse buildNoActiveLoaderResponse() {
        return GetLoaderForBucketResponse.newBuilder().setNoActiveLoader(NO_ACTIVE_LOADER).build();
    }

}
