package com.kamus.loaderconfig.db.repository;

import com.kamus.loaderconfig.db.model.DistributedBucket;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistributedBucketRepository extends JpaRepository<DistributedBucket, Integer> {

    Optional<DistributedBucket.LoaderIdProjection> findByBucketId(int bucketId);

}
