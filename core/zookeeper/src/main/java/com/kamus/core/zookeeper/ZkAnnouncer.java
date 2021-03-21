package com.kamus.core.zookeeper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.api.AsyncCuratorFrameworkDsl;
import org.apache.curator.x.async.api.CreateOption;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class ZkAnnouncer {

    private static final Logger logger = LoggerFactory.getLogger(ZkAnnouncer.class);

    private final CuratorFramework curatorFramework;
    private final AsyncCuratorFrameworkDsl zkClient;

    private final ZkAnnouncement announcement;

    private ZkAnnouncer(String zkUrl, RetryPolicy retryPolicy, ZkAnnouncement announcement) {
        this.curatorFramework = createCurator(zkUrl, retryPolicy);
        this.zkClient = AsyncCuratorFramework.wrap(this.curatorFramework);

        this.announcement = announcement;
    }

    public static ZkAnnouncerBuilder newBuilder(String zkUrl, ZkAnnouncement announcement) {
        return new ZkAnnouncerBuilder(zkUrl, announcement);
    }

    public void disconnect() {
        curatorFramework.close();
    }

    public static class ZkAnnouncerBuilder {

        private final String zkUrl;
        private RetryPolicy retryPolicy = new RetryNTimes(3, 100);
        private final ZkAnnouncement announcement;

        public ZkAnnouncerBuilder(String zkUrl, ZkAnnouncement announcement) {
            Preconditions.checkNotNull(zkUrl);
            Preconditions.checkNotNull(announcement);

            this.zkUrl = zkUrl;
            this.announcement = announcement;
        }

        public ZkAnnouncerBuilder withRetryPolicy(RetryPolicy retryPolicy) {
            Preconditions.checkNotNull(retryPolicy);

            this.retryPolicy = retryPolicy;
            return this;
        }

        public ZkAnnouncer build() {
            return new ZkAnnouncer(zkUrl, retryPolicy, announcement);
        }

    }

    private CuratorFramework createCurator(String zkUrl, RetryPolicy retryPolicy) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkUrl, retryPolicy);
        client.getConnectionStateListenable().addListener(createStateListener());
        client.start();

        return client;
    }

    private ConnectionStateListener createStateListener() {
        return (client, newState) -> {
            if (newState.isConnected() || newState.equals(ConnectionState.RECONNECTED)) {
                logger.info("State {}, announcing loader to Zookeeper", newState);

                try {
                    zkClient.create()
                            .withOptions(ImmutableSet.of(CreateOption.createParentsIfNeeded), CreateMode.EPHEMERAL_SEQUENTIAL)
                            .forPath(announcement.getPath(), announcement.getData())
                            .thenAccept(s -> logger.info("Announced at: " + s))
                            .exceptionally(t -> {
                                logger.error(t.getMessage(), t);
                                return null;
                            })
                            .toCompletableFuture()
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Got an exception {}, while trying to announce loader to Zookeeper", e.toString());
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
