package com.kamus.core.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class Endpoints {

    private final Map<String, String> endpoints;

    public Endpoints(Map<String, String> endpoints) {
        this.endpoints = Map.copyOf(endpoints);
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }
}
