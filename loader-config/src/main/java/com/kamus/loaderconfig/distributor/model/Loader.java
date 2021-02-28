package com.kamus.loaderconfig.distributor.model;

import com.google.common.base.Preconditions;
import com.kamus.core.zookeeper.Endpoints;

import java.util.Objects;

public class Loader {

    private final LoaderId id;
    private final Endpoints endpoints;

    public Loader(String path, Endpoints endpoints) {
        this(new LoaderId(path), endpoints);
    }

    public Loader(LoaderId loaderId, Endpoints endpoints) {
        Preconditions.checkNotNull(loaderId);
        Preconditions.checkNotNull(endpoints);

        this.id = loaderId;
        this.endpoints = endpoints;
    }

    public LoaderId getId() {
        return id;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loader loader = (Loader) o;
        return Objects.equals(id, loader.id) &&
                       Objects.equals(endpoints, loader.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, endpoints);
    }

    @Override
    public String toString() {
        return "Loader{" +
                       "id=" + id +
                       ", endpoints=" + endpoints +
                       '}';
    }
}
