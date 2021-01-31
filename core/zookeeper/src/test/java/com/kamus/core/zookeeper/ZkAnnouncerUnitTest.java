package com.kamus.core.zookeeper;

import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZkAnnouncerUnitTest {

    private TestingServer zkServer;

    private ZkAnnouncer zkAnnouncer;

    @BeforeEach
    public void setUp() throws Exception {
        zkServer = new TestingServer(2181, true);
        zkAnnouncer = ZkAnnouncer.newBuilder(zkServer.getConnectString())
                              .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        zkServer.stop();
    }

    @Test
    public void shouldAnnounce() {
        zkAnnouncer.announce(new ZkAnnouncement());
    }

}
