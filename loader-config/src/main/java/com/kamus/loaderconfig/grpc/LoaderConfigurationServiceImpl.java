package com.kamus.loaderconfig.grpc;

import com.google.common.base.Preconditions;
import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.grpcjava.*;
import com.kamus.loaderconfig.service.TrackedRepositoriesService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

@Service
public class LoaderConfigurationServiceImpl extends LoaderConfigurationServiceGrpc.LoaderConfigurationServiceImplBase {

    private final TrackedRepositoriesService trackedRepositoriesService;

    public LoaderConfigurationServiceImpl(TrackedRepositoriesService trackedRepositoriesService) {
        this.trackedRepositoriesService = trackedRepositoriesService;
    }

    @Override
    public void trackRepository(TrackRepositoryRequest request, StreamObserver<TrackRepositoryResponse> responseObserver) {
        try {
            Repository repository = request.getRepository();
            validateRepository(repository);

            trackedRepositoriesService.trackRepository(repository);

            responseObserver.onNext(TrackRepositoryResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }
    }

    @Override
    public void untrackRepository(UntrackRepositoryRequest request, StreamObserver<UntrackRepositoryResponse> responseObserver) {
        try {
            Repository repository = request.getRepository();
            validateRepository(repository);

            trackedRepositoriesService.untrackRepository(repository);

            responseObserver.onNext(UntrackRepositoryResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }
    }

    @Override
    public void getTrackedRepositories(GetTrackedRepositoriesRequest request, StreamObserver<GetTrackedRepositoriesResponse> responseObserver) {
        GetTrackedRepositoriesResponse response = GetTrackedRepositoriesResponse
                                                          .newBuilder()
                                                          .addAllRepository(trackedRepositoriesService.getTrackedRepositories())
                                                          .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getLoaderConfiguration(GetLoaderConfigurationRequest request, StreamObserver<GetLoaderConfigurationResponse> responseObserver) {
        super.getLoaderConfiguration(request, responseObserver);
    }

    private void validateRepository(Repository repository) {
        Preconditions.checkArgument(Strings.isNotBlank(repository.getName()), Strings.isNotBlank(repository.getOwner()));
    }

}
