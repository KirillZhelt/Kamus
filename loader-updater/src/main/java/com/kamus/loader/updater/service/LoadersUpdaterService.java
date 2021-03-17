package com.kamus.loader.updater.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.kamus.core.model.Loader;
import com.kamus.dataloader.grpcjava.AddRepositoryRequest;
import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.loader.updater.grpc.DataLoaderStubFactory;
import com.kamus.loaderconfig.grpcjava.AssignedRepository;
import org.springframework.stereotype.Service;

import static com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc.SERVICE_NAME;

@Service
public class LoadersUpdaterService {

    // have a cache of stubs

    private final DataLoaderStubFactory stubFactory;

    private final LoadingCache<Loader, DataLoaderServiceGrpc.DataLoaderServiceBlockingStub> stubsCache =
            CacheBuilder.newBuilder()
                    .maximumSize(10)
                    .build(new CacheLoader<>() {
                        @Override
                        public DataLoaderServiceGrpc.DataLoaderServiceBlockingStub load(Loader key) {
                            return LoadersUpdaterService.this.loadStub(key);
                        }
                    });

    public LoadersUpdaterService(DataLoaderStubFactory stubFactory) {
        this.stubFactory = stubFactory;
    }

    public void updateLoaderWithRepository(Loader loader, AssignedRepository repository) {
        DataLoaderServiceGrpc.DataLoaderServiceBlockingStub stub = stubsCache.getUnchecked(loader);

        AddRepositoryRequest request = AddRepositoryRequest.newBuilder()
                                               .setBucket(repository.getBucketId())
                                               .setRepository(repository.getRepository())
                                               .build();

        stub.addRepository(request);
    }

    private DataLoaderServiceGrpc.DataLoaderServiceBlockingStub loadStub(Loader loader) {
        return stubFactory.newBlockingStub(loader.getEndpoints().getEndpoints().get(SERVICE_NAME));
    }

}
