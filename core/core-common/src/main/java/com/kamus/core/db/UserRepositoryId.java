package com.kamus.core.db;

import com.google.common.base.Preconditions;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserRepositoryId implements Serializable {

    @Embedded
    private RepositoryId repositoryId;

    private Long userId;

    protected UserRepositoryId() {}

    public UserRepositoryId(String owner, String name, Long userId) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(userId);

        this.repositoryId = new RepositoryId(owner, name);
        this.userId = userId;
    }

    public RepositoryId getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(RepositoryId repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getOwner() {
        return repositoryId.getOwner();
    }

    public String getName() {
        return repositoryId.getName();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRepositoryId that = (UserRepositoryId) o;
        return Objects.equals(repositoryId, that.repositoryId) &&
                       Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryId, userId);
    }
}
