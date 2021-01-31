package com.kamus.core.spring.grpc;

import org.springframework.context.ApplicationEvent;

public class GrpcServerStartedEvent extends ApplicationEvent {

    public GrpcServerStartedEvent(Object source) {
        super(source);
    }

}
