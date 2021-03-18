package com.kamus.loaderconfig.service;

import com.google.common.base.Preconditions;
import com.kamus.core.model.BucketId;
import com.kamus.core.model.LoaderId;
import com.kamus.loaderconfig.db.model.DistributedBucket;
import com.kamus.loaderconfig.db.repository.DistributedBucketRepository;
import com.kamus.loaderconfig.distributor.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class BucketsService {

    private final DistributedBucketRepository distributedBucketRepository;
    private final int bucketCount;

    private final DistributedBucket.IdAscendingComparator bucketsComparator =
            new DistributedBucket.IdAscendingComparator();

    public BucketsService(DistributedBucketRepository distributedBucketRepository, int bucketCount) {
        this.distributedBucketRepository = distributedBucketRepository;
        this.bucketCount = bucketCount;
    }

    public void saveBucketsDistribution(Map<LoaderId, BucketsDistribution> distribution) {
        List<DistributedBucket> buckets = distribution.entrySet()
                                                  .stream()
                                                  .flatMap(e -> fromDistribution(e.getKey(), e.getValue()))
                                                  .collect(Collectors.toList());
        distributedBucketRepository.saveAll(buckets);
        distributedBucketRepository.flush();
    }

    private Stream<DistributedBucket> fromDistribution(LoaderId loaderId, BucketsDistribution distribution) {
        AssignedBucketsInterval assignedBuckets = distribution.getAssignedBuckets();
        return IntStream.range(assignedBuckets.getStartBucket(), assignedBuckets.getEndBucket())
                       .mapToObj(bucketId -> new DistributedBucket(bucketId, loaderId.getId()));
    }

    public List<DistributedBucket> getDistributedBucketsInBucketOrder() {
        return distributedBucketRepository.findAll();
    }

    public Map<LoaderId, SortedSet<DistributedBucket>> getBucketsForLoaders() {
        List<DistributedBucket> distributedBuckets = getDistributedBucketsInBucketOrder();

        return distributedBuckets.stream().collect(Collectors.toMap(
                b -> new LoaderId(b.getLoaderId()),
                this::treeSetOf,
                (b1, b2) -> {
                    b1.addAll(b2);
                    return b1;
                }));
    }

    public LoaderId getLoaderForBucket(BucketId bucketId) {
        Preconditions.checkArgument(bucketId.getBucketId() >= 0);
        Preconditions.checkArgument(bucketId.getBucketId() < bucketCount);

        return distributedBucketRepository.findByBucketId(bucketId.getBucketId())
                       .map(DistributedBucket.LoaderIdProjection::getLoaderId)
                       .map(LoaderId::new)
                       .orElseThrow(() -> new IllegalStateException("Unreachable"));
    }

    public void clearDistribution() {
        distributedBucketRepository.deleteAll();
    }

    private TreeSet<DistributedBucket> treeSetOf(DistributedBucket bucket) {
        TreeSet<DistributedBucket> set = new TreeSet<>(bucketsComparator);
        set.add(bucket);
        return set;
    }

}
