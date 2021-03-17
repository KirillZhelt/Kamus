package com.kamus.loaderconfig.db.repository;

import com.kamus.core.db.RepositoryId;
import com.kamus.loaderconfig.db.model.BucketRepositoryCount;
import com.kamus.loaderconfig.db.model.TrackedRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface RepositoriesRepository extends CrudRepository<TrackedRepository, RepositoryId> {

    List<TrackedRepository> findAllByTrackedTrue();
    List<TrackedRepository> findAllByBucketId(int bucketId);
    Set<TrackedRepository.RepositoryIdView> findByIdIn(List<RepositoryId> ids);

    @Query("SELECT r.bucketId AS bucketId, COUNT(r) AS repositoryCount " +
                   "FROM TrackedRepository r " +
                   "GROUP BY r.bucketId " +
                   "ORDER BY r.bucketId")
    List<BucketRepositoryCount> getRepositoryCountPerBucket();

}
