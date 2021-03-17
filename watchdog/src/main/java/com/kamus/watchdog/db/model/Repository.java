package com.kamus.watchdog.db.model;

import com.google.common.base.Preconditions;
import com.kamus.core.db.RepositoryId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Repository {

    @EmbeddedId
    private RepositoryId id;

    @ManyToOne
    private User user;

    protected Repository() {}

    public Repository(String owner, String name, User user) {
        Preconditions.checkNotNull(user);

        this.id = new RepositoryId(owner, name);
        this.user = user;
    }

    public RepositoryId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
}
