package com.kamus.loaderconfig.grpc;

import io.grpc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerRunner.class);

    private static final String AWAIT_THREAD_NAME = "grpc-server-await-thread";

    private static final long SHUTDOWN_GRACE = 5;
    private static final TimeUnit SHUTDOWN_GRACE_UNIT = TimeUnit.SECONDS;

    private final Server grpcServer;
    private final CountDownLatch latch;

    public GrpcServerRunner(Server grpcServer) {
        this.grpcServer = grpcServer;

        this.latch = new CountDownLatch(1);
    }

    @Override
    public void run(String... args) throws Exception {
        grpcServer.start();
        logger.info("gRPC server is started!");

        startAwaitThread();
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Shutting down gRPC server...");

        try {
            grpcServer.shutdown();
            grpcServer.awaitTermination(SHUTDOWN_GRACE, SHUTDOWN_GRACE_UNIT);
        } finally {
            latch.countDown();
        }

        logger.info("gRPC server is stopped.");
    }

    private void startAwaitThread() {
        Thread awaitThread = new Thread(() -> {
            try {
                latch.await();

                logger.info(AWAIT_THREAD_NAME + " is terminating.");
            } catch (InterruptedException ex) {
                logger.info(AWAIT_THREAD_NAME + " was interrupted.");
                Thread.currentThread().interrupt();
            }
        }, AWAIT_THREAD_NAME);

        awaitThread.start();
    }

}
