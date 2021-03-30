package com.kamus.watchdog.db.repository;

import com.kamus.core.db.UserRepositoryId;
import com.kamus.core.db.User;
import com.kamus.watchdog.db.model.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepositoriesRepository extends JpaRepository<UserRepository, UserRepositoryId> {

    List<UserRepository> findAllByUser(User user);

}
