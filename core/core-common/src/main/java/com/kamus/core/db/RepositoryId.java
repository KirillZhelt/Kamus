package com.kamus.core.db;

import com.google.common.base.Preconditions;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RepositoryId implements Serializable {

    @NotNull
    private String owner;

    @NotNull
    private String name;

    protected RepositoryId() {

    }

    public RepositoryId(String owner, String name) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(name);

        this.owner = owner;
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryId that = (RepositoryId) o;
        return owner.equals(that.owner) &&
                       name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }
}
