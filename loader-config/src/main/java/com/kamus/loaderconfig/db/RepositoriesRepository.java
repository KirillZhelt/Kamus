package com.kamus.loaderconfig.db;

import com.kamus.loaderconfig.db.model.RepositoryId;
import com.kamus.loaderconfig.db.model.TrackedRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RepositoriesRepository extends CrudRepository<TrackedRepository, RepositoryId> {

    List<TrackedRepository> findAllByTrackedTrue();

}
