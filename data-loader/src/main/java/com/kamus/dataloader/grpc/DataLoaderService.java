package com.kamus.dataloader.grpc;

import com.kamus.dataloader.grpcjava.*;
import com.kamus.dataloader.service.LoaderConfigurationService;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class DataLoaderService extends DataLoaderServiceGrpc.DataLoaderServiceImplBase {

    private final LoaderConfigurationService loaderConfigurationService;

    public DataLoaderService(LoaderConfigurationService loaderConfigurationService) {
        this.loaderConfigurationService = loaderConfigurationService;
    }

    @Override
    public void getCurrentDistributionHash(GetCurrentDistributionHashRequest request, StreamObserver<GetCurrentDistributionHashResponse> responseObserver) {
        int hash = loaderConfigurationService.countCurrentConfigurationHash();
        responseObserver.onNext(GetCurrentDistributionHashResponse.newBuilder().setHash(hash).build());
        responseObserver.onCompleted();
    }

    @Override
    public void setSources(SetSourcesRequest request, StreamObserver<SetSourcesResponse> responseObserver) {
        loaderConfigurationService.setSources(request.getConfigurationBucketList());
        responseObserver.onNext(SetSourcesResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void addRepository(AddRepositoryRequest request, StreamObserver<AddRepositoryResponse> responseObserver) {
        loaderConfigurationService.addRepository(request.getBucket(), request.getRepository());
        responseObserver.onNext(AddRepositoryResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeRepository(AddRepositoryRequest request, StreamObserver<AddRepositoryResponse> responseObserver) {
        super.removeRepository(request, responseObserver);
    }

    @Override
    public void addRepositories(AddRepositoriesRequest request, StreamObserver<AddRepositoriesResponse> responseObserver) {
        super.addRepositories(request, responseObserver);
    }

    @Override
    public void removeRepositories(RemoveRepositoriesRequest request, StreamObserver<RemoveRepositoriesResponse> responseObserver) {
        super.removeRepositories(request, responseObserver);
    }
}
