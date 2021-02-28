package com.kamus.loaderconfig.service;

import com.kamus.loaderconfig.db.model.DistributedBucket;
import com.kamus.loaderconfig.db.repository.DistributedBucketRepository;
import com.kamus.loaderconfig.distributor.model.AssignedBucketsInterval;
import com.kamus.loaderconfig.distributor.model.BucketsDistribution;
import com.kamus.loaderconfig.distributor.model.LoaderId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class BucketsService {

    private final DistributedBucketRepository distributedBucketRepository;

    private final DistributedBucket.IdAscendingComparator bucketsComparator =
            new DistributedBucket.IdAscendingComparator();

    public BucketsService(DistributedBucketRepository distributedBucketRepository) {
        this.distributedBucketRepository = distributedBucketRepository;
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

    private TreeSet<DistributedBucket> treeSetOf(DistributedBucket bucket) {
        TreeSet<DistributedBucket> set = new TreeSet<>(bucketsComparator);
        set.add(bucket);
        return set;
    }

}
