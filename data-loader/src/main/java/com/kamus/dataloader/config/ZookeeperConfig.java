package com.kamus.dataloader.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.api.AsyncCuratorFrameworkDsl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {

    private static final int SLEEP_MS_BETWEEN_RETRIES = 100;
    private static final int MAX_RETRIES = 3;

    @Bean
    public AsyncCuratorFrameworkDsl zkClient(@Value("${zookeeper.url}") String zookeeperUrl) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperUrl, retryPolicy());
        client.start();
        return AsyncCuratorFramework.wrap(client);
    }

    @Bean
    RetryPolicy retryPolicy() {
        return new RetryNTimes(MAX_RETRIES, SLEEP_MS_BETWEEN_RETRIES);
    }

}
