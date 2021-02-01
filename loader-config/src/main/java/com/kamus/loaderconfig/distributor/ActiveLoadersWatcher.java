package com.kamus.loaderconfig.distributor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.kamus.core.zookeeper.Endpoints;
import com.kamus.loaderconfig.distributor.model.Loader;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ActiveLoadersWatcher {

    private static final Logger logger = LoggerFactory.getLogger(ActiveLoadersWatcher.class);

    private static final String LOADERS_PATH = "/announcements/data-loader";

    private final CuratorFramework zkClient;
    private final PathChildrenCache loadersCache;

    private final Set<LoadersChangeAware> subscribers = new HashSet<>();

    private final ObjectMapper objectMapper;

    public ActiveLoadersWatcher(@Value("${zookeeper.url}") String zkUrl, ObjectMapper objectMapper) {
        this.zkClient = createZkClient(zkUrl, new RetryNTimes(5, 500));
        this.loadersCache = new PathChildrenCache(zkClient, LOADERS_PATH, true);

        this.objectMapper = objectMapper;

        setupCache();
    }

    @PostConstruct
    public void startWatching() throws Exception {
        loadersCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

    public void subscribe(LoadersChangeAware subscriber) {
        Preconditions.checkNotNull(subscriber);

        subscribers.add(subscriber);
    }

    public void unsubscribe(LoadersChangeAware subscriber) {
        Preconditions.checkNotNull(subscriber);

        subscribers.remove(subscriber);
    }

    @PreDestroy
    public void stopWatching() throws IOException {
        loadersCache.close();
        zkClient.close();
    }

    private void setupCache() {
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case INITIALIZED: {
                        Set<Loader> loaders = event.getInitialData().stream()
                                                      .map(childData -> {
                                                          try {
                                                              return loaderFromEvent(childData);
                                                          } catch (Exception ex) {
                                                              logger.error("An exception was thrown while deserializing Loader: " + ex.toString());
                                                              return null;
                                                          }
                                                      })
                                                      .filter(Objects::nonNull)
                                                      .collect(Collectors.toSet());

                        logger.info("data-loaders are initialized: " + loaders);
                        subscribers.forEach(subscriber -> subscriber.onLoadersInit(loaders));

                        break;
                    }

                    case CHILD_ADDED: {
                        Loader loader = loaderFromEvent(event.getData());
                        logger.info("New data-loader is added: " + loader);
                        subscribers.forEach(subscriber -> subscriber.onLoaderAdded(loader));

                        break;
                    }

                    case CHILD_REMOVED: {
                        String path = event.getData().getPath();
                        logger.info("data-loader is removed: " + path);
                        subscribers.forEach(subscriber -> subscriber.onLoaderRemoved(path));

                        break;
                    }

                    case CHILD_UPDATED: {
                        logger.error("data-loader is updated: " + event.getData().getPath());
                        throw new IllegalStateException("data-loader is updated: " + event.getData().getPath());
                    }
                }
            }
        };

        loadersCache.getListenable().addListener(listener);
    }

    private CuratorFramework createZkClient(String zookeeperUrl, RetryPolicy retryPolicy) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperUrl, retryPolicy);
        client.start();
        return client;
    }

    private Loader loaderFromEvent(ChildData childData) throws Exception {
        String path = childData.getPath();
        Endpoints endpoints = objectMapper.readValue(childData.getData(), Endpoints.class);

        return new Loader(path, endpoints);
    }

}
