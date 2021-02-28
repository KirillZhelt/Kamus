package com.kamus.loaderconfig.distributor;

import com.google.common.base.Preconditions;
import com.kamus.loaderconfig.db.model.DistributedBucket;
import com.kamus.loaderconfig.distributor.model.AssignedBucketsInterval;
import com.kamus.loaderconfig.distributor.model.BucketsDistribution;
import com.kamus.loaderconfig.distributor.model.LoaderId;
import com.kamus.loaderconfig.service.BucketsService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BucketsDistributor {

    private final BucketsService bucketsService;
    private final int bucketCount;

    public BucketsDistributor(BucketsService bucketsService, int bucketCount) {
        Preconditions.checkArgument(bucketCount > 0, "Buckets count should be positive");

        this.bucketsService = bucketsService;
        this.bucketCount = bucketCount;
    }

    public Map<LoaderId, BucketsDistribution> distributeOnLoadersInit(Set<LoaderId> activeLoaders) {
        List<DistributedBucket> distributedBuckets = this.bucketsService.getDistributedBucketsInBucketOrder();

        Set<LoaderId> savedLoaders = distributedBuckets.stream()
                                             .map(DistributedBucket::getLoaderId)
                                             .map(LoaderId::new)
                                             .collect(Collectors.toSet());

        // buckets count changed or set of active loaders changed
        if (distributedBuckets.size() != bucketCount || !savedLoaders.equals(activeLoaders)) {
            return distribute(activeLoaders);
        } else {
            return bucketsService.getBucketsForLoaders()
                           .entrySet()
                           .stream()
                           .collect(Collectors.toMap(
                                   Map.Entry::getKey,
                                   e -> BucketsDistribution.newOnInitBucketsDistribution(AssignedBucketsInterval.fromSet(e.getValue()))
                           ));
        }
    }

    public Map<LoaderId, BucketsDistribution> distributeOnLoaderAdded(LoaderId loader) {
        return null;
    }

    public Map<LoaderId, BucketsDistribution> distributeOnLoaderRemoved(LoaderId loader) {
        return null;
    }

    private Map<LoaderId, BucketsDistribution> distribute(Set<LoaderId> activeLoaders) {
        Map<LoaderId, BucketsDistribution> distribution = new HashMap<>();

        int bucketsCountForEachLoader = bucketCount / activeLoaders.size();
        int bucketsLeft = bucketCount % activeLoaders.size();

        int currentStartBucket = 0;

        for (LoaderId loaderId : activeLoaders) {
            int endBucket = currentStartBucket + bucketsCountForEachLoader;
            if (bucketsLeft > 0) {
                endBucket += 1;
                bucketsLeft--;
            }

            BucketsDistribution distributionForLoader = BucketsDistribution.newOnInitBucketsDistribution(
                    new AssignedBucketsInterval(currentStartBucket, endBucket)
            );
            distribution.put(loaderId, distributionForLoader);

            currentStartBucket = endBucket;
        }

        this.bucketsService.saveBucketsDistribution(distribution);

        return distribution;
    }

}
