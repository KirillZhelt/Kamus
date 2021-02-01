package com.kamus.loaderconfig.grpc;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc.DataLoaderServiceFutureStub;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class DataLoaderStubFactory {

    public DataLoaderServiceFutureStub newFutureStub(String serviceUrl) {
        Channel channel = ManagedChannelBuilder.forTarget(serviceUrl)
                .usePlaintext()
                .build();

        return DataLoaderServiceGrpc.newFutureStub(channel);
    }

}
