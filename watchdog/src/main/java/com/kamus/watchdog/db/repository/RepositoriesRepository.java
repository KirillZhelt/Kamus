package com.kamus.watchdog.db.repository;

import com.kamus.core.db.RepositoryId;
import com.kamus.watchdog.db.model.Repository;
import com.kamus.watchdog.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoriesRepository extends JpaRepository<Repository, RepositoryId> {

    List<Repository> findAllByUser(User user);

}
