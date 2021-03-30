package com.kamus.watchdog.db.model;

import com.kamus.core.db.User;
import com.kamus.core.db.UserRepositoryId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
public class UserRepository {

    @EmbeddedId
    private UserRepositoryId userRepositoryId;

    @MapsId("userId")
    @ManyToOne
    private User user;

    protected UserRepository() {}

    public UserRepository(String owner, String name, User user) {
        this.userRepositoryId = new UserRepositoryId(owner, name, user.getId());
        this.user = user;
    }

    public UserRepositoryId getId() {
        return userRepositoryId;
    }

    public User getUser() {
        return user;
    }
}
