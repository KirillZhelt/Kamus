package com.kamus.dataloader.grpc;

import com.kamus.dataloader.grpcjava.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class DataLoaderServiceImpl extends DataLoaderServiceGrpc.DataLoaderServiceImplBase {

    @Override
    public void setSources(LoaderConfigurationRequest request, StreamObserver<LoaderConfigurationResponse> responseObserver) {
        super.setSources(request, responseObserver);
    }

    @Override
    public void addRepository(AddRepositoryRequest request, StreamObserver<AddRepositoryResponse> responseObserver) {
        super.addRepository(request, responseObserver);
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
