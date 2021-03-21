package com.kamus.dataloader.config;

import com.kamus.core.spring.grpc.GrpcServerStartedEvent;
import com.kamus.core.zookeeper.Endpoints;
import com.kamus.core.zookeeper.EndpointsZkAnnouncement;
import com.kamus.core.zookeeper.ZkAnnouncer;
import io.grpc.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ZookeeperConfig {

    @Value("${service.name:data-loader}")
    private String serviceName;

    @Value("${zookeeper.url}")
    private String zkUrl;

    private final Server server;

    public ZookeeperConfig(Server server) {
        this.server = server;
    }

    @EventListener
    public void announceGrpcServices(GrpcServerStartedEvent serverStarted) throws Exception {
        String url = InetAddress.getLocalHost().getHostAddress() + ":" + server.getPort();

        Map<String, String> endpoints = server.getServices()
                                                .stream()
                                                .collect(Collectors.toMap(service -> service.getServiceDescriptor().getName(),
                                                                          service -> url));
        Endpoints endpointsToAnnounce = new Endpoints(endpoints);

        ZkAnnouncer.newBuilder(zkUrl, new EndpointsZkAnnouncement(serviceName, endpointsToAnnounce))
                .build();
    }

}
