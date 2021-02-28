package com.kamus.loaderconfig.distributor.model;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class LoaderId {

    private final String path;

    public LoaderId(String path) {
        Preconditions.checkNotNull(path);

        this.path = path;
    }

    public String getId() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoaderId loaderId = (LoaderId) o;
        return Objects.equals(path, loaderId.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "LoaderId{" +
                       "path='" + path + '\'' +
                       '}';
    }

}
