package com.kamus.core.zookeeper;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class BaseZkAnnouncement implements ZkAnnouncement {

    protected final String path;
    protected final byte[] data;

    public BaseZkAnnouncement(String path, byte[] data) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(data);

        this.path = path;
        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "BaseZkAnnouncement{" +
                       "path='" + path + '\'' +
                       ", data=" + new String(data) +
                       '}';
    }
}
