package com.kamus.loaderconfig.distributor.model;

import com.google.common.base.Preconditions;
import com.kamus.core.zookeeper.Endpoints;

import java.util.Objects;

public class Loader {

    private final String path;
    private final Endpoints endpoints;

    public Loader(String path, Endpoints endpoints) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(endpoints);

        this.path = path;
        this.endpoints = endpoints;
    }

    public String getPath() {
        return path;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loader loader = (Loader) o;
        return path.equals(loader.path) &&
                       endpoints.equals(loader.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, endpoints);
    }

    @Override
    public String toString() {
        return "Loader{" +
                       "path='" + path + '\'' +
                       ", endpoints=" + endpoints +
                       '}';
    }

}
