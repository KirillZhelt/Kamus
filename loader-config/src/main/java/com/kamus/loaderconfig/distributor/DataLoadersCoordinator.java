package com.kamus.loaderconfig.distributor;

import com.kamus.dataloader.grpcjava.DataLoaderServiceGrpc.DataLoaderServiceFutureStub;
import com.kamus.loaderconfig.grpc.DataLoaderStubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataLoadersCoordinator {

    private final DataLoaderStubFactory stubFactory;

    private final Map<String, DataLoaderServiceFutureStub> loaderStubs = new HashMap<>();

    @Autowired
    public DataLoadersCoordinator(DataLoaderStubFactory stubFactory) {
        this.stubFactory = stubFactory;
    }

}
