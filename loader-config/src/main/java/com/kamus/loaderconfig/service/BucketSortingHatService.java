package com.kamus.loaderconfig.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.db.model.BucketRepositoryCount;
import com.kamus.loaderconfig.db.repository.RepositoriesRepository;
import com.kamus.core.model.BucketId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class BucketSortingHatService {

    private static final Logger logger = LoggerFactory.getLogger(BucketSortingHatService.class);

    private final RepositoriesRepository repositoriesRepository;
    private final int bucketCount;

    private final PriorityQueue<WeightedBucket> bucketsLoad;

    public BucketSortingHatService(RepositoriesRepository repositoriesRepository, int bucketCount) {
        this.repositoriesRepository = repositoriesRepository;
        this.bucketCount = bucketCount;

        this.bucketsLoad = loadBuckets();
    }

    public BucketId assign(List<Repository> repositories) {
        WeightedBucket leastLoadedBucket = bucketsLoad.poll();

        if (Objects.isNull(leastLoadedBucket)) {
            throw new IllegalStateException("PriorityQueue should always return non null bucket");
        }

        bucketsLoad.add(new WeightedBucket(leastLoadedBucket.getBucketId(), leastLoadedBucket.weight + 1));

        logger.info("Assigning repositories: " + repositories + " to bucket #" + leastLoadedBucket.getBucketId());

        return leastLoadedBucket.getBucketId();
    }

    private PriorityQueue<WeightedBucket> loadBuckets() {
        PriorityQueue<WeightedBucket> result = new PriorityQueue<>(bucketCount);

        List<BucketRepositoryCount> bucketRepositoryCounts = repositoriesRepository.getRepositoryCountPerBucket();

        Map<BucketId, Integer> weightsForBuckets = bucketRepositoryCounts.stream()
                                                           .collect(Collectors.toMap(
                                                                   b -> new BucketId(b.getBucketId()),
                                                                   BucketRepositoryCount::getRepositoryCount));

        IntStream.range(0, bucketCount).forEach(id -> {
            BucketId bucketId = new BucketId(id);
            int weight = weightsForBuckets.getOrDefault(bucketId, 0);

            result.add(new WeightedBucket(bucketId, weight));
        });

        return result;
    }

    private static class WeightedBucket implements Comparable<WeightedBucket> {

        private final BucketId bucketId;
        private final int weight;

        public WeightedBucket(BucketId bucketId, int weight) {
            this.bucketId = bucketId;
            this.weight = weight;
        }

        public BucketId getBucketId() {
            return bucketId;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public int compareTo(@NotNull WeightedBucket o) {
            return Integer.compare(weight, o.weight);
        }
    }

}
