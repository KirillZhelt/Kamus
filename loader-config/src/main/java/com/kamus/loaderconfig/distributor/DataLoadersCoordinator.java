package com.kamus.loaderconfig.distributor;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc.DataLoaderServiceFutureStub;
import com.kamus.loaderconfig.distributor.model.BucketsDistribution;
import com.kamus.core.model.Loader;
import com.kamus.core.model.LoaderId;
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
    private final LoaderUpdater loaderUpdater;

    private final Set<Loader> activeLoaders = new HashSet<>();
    private final Map<LoaderId, DataLoaderServiceFutureStub> loaderStubs = new HashMap<>();

    @Autowired
    public DataLoadersCoordinator(DataLoaderStubFactory stubFactory, ActiveLoadersWatcher loadersWatcher,
                                  BucketsDistributor bucketsDistributor, LoaderUpdater updater) {
        this.stubFactory = stubFactory;
        this.loadersWatcher = loadersWatcher;
        this.bucketsDistributor = bucketsDistributor;
        this.loaderUpdater = updater;
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

        updateLoaders(bucketsDistributor.distributeOnLoadersInit(newLoaderStubs.keySet()));
    }

    @Override
    public void onLoaderAdded(Loader loader) {
        if (!activeLoaders.add(loader)) {
            throw new IllegalStateException("data-loader already existed: " +  loader.toString());
        }

        loaderStubs.put(loader.getId(), createDataLoaderStub(loader));

        updateLoaders(bucketsDistributor.distributeOnLoaderAdded(loader.getId(), loaderStubs.keySet()));
    }

    @Override
    public void onLoaderRemoved(LoaderId loaderId) {
        if (!activeLoaders.removeIf(loader -> loader.getId().equals(loaderId))) {
            throw new IllegalStateException("data-loader does not exist: " +  loaderId.toString());
        }

        loaderStubs.remove(loaderId);

        updateLoaders(bucketsDistributor.distributeOnLoaderRemoved(loaderId, loaderStubs.keySet()));
    }

    public Optional<Loader> getLoaderById(LoaderId id) {
        return activeLoaders.stream().filter(loader -> loader.getId().equals(id)).findFirst();
    }

    private void updateLoaders(Map<LoaderId, BucketsDistribution> distributionMap) {
        distributionMap.forEach((id, distribution) -> {
            loaderUpdater.updateLoader(loaderStubs.get(id), distribution);
        });
    }

    private DataLoaderServiceFutureStub createDataLoaderStub(Loader loader) {
        return stubFactory.newFutureStub(loader.getEndpoints().getEndpoints().get(DataLoaderServiceGrpc.SERVICE_NAME));
    }

}
