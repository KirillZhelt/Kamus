package com.kamus.core.zookeeper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Endpoints {

    private final Map<String, String> endpoints;

    public Endpoints(@JsonProperty("endpoints") Map<String, String> endpoints) {
        this.endpoints = Map.copyOf(endpoints);
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return "Endpoints{" +
                       "endpoints=" + endpoints +
                       '}';
    }
}
