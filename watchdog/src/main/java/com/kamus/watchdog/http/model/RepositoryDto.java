package com.kamus.watchdog.http.model;

import com.google.common.base.Preconditions;

public class RepositoryDto {

    private final String name;
    private final String owner; // The login field of a user or organization.

    public RepositoryDto(String name, String owner) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(owner);

        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

}
