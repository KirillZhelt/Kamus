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

    private final CuratorFramework curatorFramework;

    private final AsyncCuratorFrameworkDsl zkClient;

    private ZkAnnouncer(String zkUrl, RetryPolicy retryPolicy) {
        this.curatorFramework = createCurator(zkUrl, retryPolicy);
        this.zkClient = AsyncCuratorFramework.wrap(this.curatorFramework);
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

        // TODO: handle reconnect event
    }

    public void disconnect() {
        curatorFramework.close();
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

    private CuratorFramework createCurator(String zkUrl, RetryPolicy retryPolicy) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkUrl, retryPolicy);
        client.start();
        return client;
    }

}
