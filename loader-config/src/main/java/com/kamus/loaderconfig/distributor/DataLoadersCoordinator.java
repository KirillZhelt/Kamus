package com.kamus.loaderconfig.distributor;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc.DataLoaderServiceFutureStub;
import com.kamus.loaderconfig.distributor.model.Loader;
import com.kamus.loaderconfig.grpc.DataLoaderStubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataLoadersCoordinator implements LoadersChangeAware {

    private final DataLoaderStubFactory stubFactory;
    private final ActiveLoadersWatcher loadersWatcher;
    private final SourcesDistributor sourcesDistributor;

    private final Map<String, DataLoaderServiceFutureStub> loaderStubs = new HashMap<>();

    @Autowired
    public DataLoadersCoordinator(DataLoaderStubFactory stubFactory, ActiveLoadersWatcher loadersWatcher,
                                  SourcesDistributor sourcesDistributor) {
        this.stubFactory = stubFactory;
        this.loadersWatcher = loadersWatcher;
        this.sourcesDistributor = sourcesDistributor;
    }

    @PostConstruct
    public void init() {
        loadersWatcher.subscribe(this);
    }

    @PreDestroy
    public void destroy() {
        loadersWatcher.unsubscribe(this);
    }

    @Override
    public void onLoadersInit(Set<Loader> loaders) {
        if (!loaderStubs.isEmpty()) {
            throw new IllegalStateException("active data-loaders map is not empty!");
        }

        Map<String, DataLoaderServiceFutureStub> newLoaderStubs =
                loaders.stream().collect(Collectors.toMap(Loader::getPath, this::createDataLoaderStub));

        loaderStubs.putAll(newLoaderStubs);
    }

    @Override
    public void onLoaderAdded(Loader loader) {
        if (Objects.nonNull(loaderStubs.put(loader.getPath(), createDataLoaderStub(loader)))) {
            throw new IllegalStateException("data-loader already existed: " +  loader.toString());
        }
    }

    @Override
    public void onLoaderRemoved(String path) {
        if (Objects.isNull(loaderStubs.remove(path))) {
            throw new IllegalStateException("data-loader with the given path doesn't exists: " + path);
        }
    }

    private DataLoaderServiceFutureStub createDataLoaderStub(Loader loader) {
        return stubFactory.newFutureStub(loader.getEndpoints().getEndpoints().get(DataLoaderServiceGrpc.SERVICE_NAME));
    }

}
