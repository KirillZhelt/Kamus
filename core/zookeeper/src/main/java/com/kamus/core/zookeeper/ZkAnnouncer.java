package com.kamus.core.zookeeper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
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

    private final AsyncCuratorFrameworkDsl zkClient;

    private ZkAnnouncer(String zkUrl, RetryPolicy retryPolicy) {
        this.zkClient = createZkClient(zkUrl, retryPolicy);
    }

    public static ZkAnnouncerBuilder newBuilder(String zkUrl) {
        return new ZkAnnouncerBuilder(zkUrl);
    }

    public void announce(ZkAnnouncement announcement) throws InterruptedException, ExecutionException {
        logger.info("Announcing to Zookeeper: " + announcement);

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
    }

    public static class ZkAnnouncerBuilder {

        private final String zkUrl;

        private RetryPolicy retryPolicy = new RetryNTimes(3, 100);

        public ZkAnnouncerBuilder(String zkUrl) {
            Preconditions.checkNotNull(zkUrl);

            this.zkUrl = zkUrl;
        }

        public ZkAnnouncerBuilder withRetryPolicy(RetryPolicy retryPolicy) {
            Preconditions.checkNotNull(retryPolicy);

            this.retryPolicy = retryPolicy;
            return this;
        }

        public ZkAnnouncer build() {
            return new ZkAnnouncer(zkUrl, retryPolicy);
        }

    }

    private AsyncCuratorFrameworkDsl createZkClient(String zookeeperUrl, RetryPolicy retryPolicy) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperUrl, retryPolicy);
        client.start();
        return AsyncCuratorFramework.wrap(client);
    }

}
