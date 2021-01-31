package com.kamus.core.zookeeper;

public interface ZkAnnouncement {

    String getPath();
    byte[] getData();

}
