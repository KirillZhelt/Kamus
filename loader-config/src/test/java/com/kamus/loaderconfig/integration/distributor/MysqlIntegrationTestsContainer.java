package com.kamus.loaderconfig.integration.distributor;

import org.testcontainers.containers.MySQLContainer;

public class MysqlIntegrationTestsContainer extends MySQLContainer<MysqlIntegrationTestsContainer> {

    public MysqlIntegrationTestsContainer() {
        super("mysql");
    }

    public static final MysqlIntegrationTestsContainer INSTANCE = new MysqlIntegrationTestsContainer();

    static {
        INSTANCE.start();
    }

    public static MysqlIntegrationTestsContainer getInstance() {
        return INSTANCE;
    }

    @Override
    public void start() {
        super.start();

        System.setProperty("DB_URL", INSTANCE.getJdbcUrl());
        System.setProperty("DB_USERNAME", INSTANCE.getUsername());
        System.setProperty("DB_PASSWORD", INSTANCE.getPassword());
    }

}
