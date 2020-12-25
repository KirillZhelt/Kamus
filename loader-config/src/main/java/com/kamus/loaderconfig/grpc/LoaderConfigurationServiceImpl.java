package com.kamus.loaderconfig.grpc;

import com.kamus.loaderconfig.grpcjava.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class LoaderConfigurationServiceImpl extends LoaderConfigurationServiceGrpc.LoaderConfigurationServiceImplBase {

    @Override
    public void addRepository(AddRepositoryRequest request, StreamObserver<AddRepositoryResponse> responseObserver) {
        responseObserver.onNext(AddRepositoryResponse.newBuilder().setAdded(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeRepository(RemoveRepositoryRequest request, StreamObserver<RemoveRepositoryResponse> responseObserver) {
        super.removeRepository(request, responseObserver);
    }

    @Override
    public void getRepositories(GetRepositoriesRequest request, StreamObserver<GetRepositoriesResponse> responseObserver) {
        super.getRepositories(request, responseObserver);
    }

    @Override
    public void getLoaderConfiguration(GetLoaderConfigurationRequest request, StreamObserver<GetLoaderConfigurationResponse> responseObserver) {
        super.getLoaderConfiguration(request, responseObserver);
    }
}
