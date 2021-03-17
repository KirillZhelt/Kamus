package com.kamus.loader.updater.grpc;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class DataLoaderStubFactory {

    public DataLoaderServiceGrpc.DataLoaderServiceBlockingStub newBlockingStub(String serviceUrl) {
        Channel channel = ManagedChannelBuilder.forTarget(serviceUrl)
                                  .usePlaintext()
                                  .build();

        return DataLoaderServiceGrpc.newBlockingStub(channel);
    }

}
