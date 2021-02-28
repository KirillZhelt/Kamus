package com.kamus.loaderconfig.distributor;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc.DataLoaderServiceFutureStub;
import com.kamus.loaderconfig.distributor.model.Loader;
import com.kamus.loaderconfig.distributor.model.LoaderId;
import com.kamus.loaderconfig.grpc.DataLoaderStubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataLoadersCoordinator implements LoadersChangeAware {

    private final DataLoaderStubFactory stubFactory;
    private final ActiveLoadersWatcher loadersWatcher;
    private final BucketsDistributor bucketsDistributor;

    private final Set<Loader> activeLoaders = new HashSet<>();
    private final Map<LoaderId, DataLoaderServiceFutureStub> loaderStubs = new HashMap<>();

    @Autowired
    public DataLoadersCoordinator(DataLoaderStubFactory stubFactory, ActiveLoadersWatcher loadersWatcher,
                                  BucketsDistributor bucketsDistributor) {
        this.stubFactory = stubFactory;
        this.loadersWatcher = loadersWatcher;
        this.bucketsDistributor = bucketsDistributor;
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
        if (!activeLoaders.isEmpty()) {
            throw new IllegalStateException("active data-loaders map is not empty!");
        }

        activeLoaders.addAll(loaders);

        Map<LoaderId, DataLoaderServiceFutureStub> newLoaderStubs =
                loaders.stream().collect(Collectors.toMap(
                        Loader::getId,
                        this::createDataLoaderStub));

        loaderStubs.putAll(newLoaderStubs);
    }

    @Override
    public void onLoaderAdded(Loader loader) {
        if (!activeLoaders.add(loader)) {
            throw new IllegalStateException("data-loader already existed: " +  loader.toString());
        }

        loaderStubs.put(loader.getId(), createDataLoaderStub(loader));
    }

    @Override
    public void onLoaderRemoved(LoaderId loaderId) {
        if (!activeLoaders.removeIf(loader -> loader.getId().equals(loaderId))) {
            throw new IllegalStateException("data-loader does not exist: " +  loaderId.toString());
        }

        loaderStubs.remove(loaderId);
    }

    private DataLoaderServiceFutureStub createDataLoaderStub(Loader loader) {
        return stubFactory.newFutureStub(loader.getEndpoints().getEndpoints().get(DataLoaderServiceGrpc.SERVICE_NAME));
    }

}
