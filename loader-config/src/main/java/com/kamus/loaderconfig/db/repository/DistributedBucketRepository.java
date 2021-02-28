package com.kamus.loaderconfig.db.repository;

import com.kamus.loaderconfig.db.model.DistributedBucket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributedBucketRepository extends JpaRepository<DistributedBucket, Integer> {

}
