package com.kamus.loaderconfig.distributor.model;

import com.google.common.base.Preconditions;
import com.kamus.loaderconfig.db.model.DistributedBucket;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;

public class AssignedBucketsInterval implements AssignedBuckets {

    // inclusive
    private final int startBucket;

    // exclusive
    private final int endBucket;

    public AssignedBucketsInterval(int startBucket, int endBucket) {
        Preconditions.checkArgument(startBucket >= 0, "Bucket number should be positive");
        Preconditions.checkArgument(endBucket >= 0, "Bucket number should be positive");

        this.startBucket = startBucket;
        this.endBucket = endBucket;
    }

    private AssignedBucketsInterval(SortedSet<DistributedBucket> buckets) {
        this(buckets.first().getBucketId(), buckets.last().getBucketId());
    }

    public static AssignedBucketsInterval fromSet(SortedSet<DistributedBucket> buckets) {
        checkSerialInterval(buckets);
        return new AssignedBucketsInterval(buckets);
    }

    public int getStartBucket() {
        return startBucket;
    }

    public int getEndBucket() {
        return endBucket;
    }

    @Override
    @Nonnull
    public Iterator<Integer> iterator() {
        return new IntervalIterator(startBucket, endBucket);
    }

    private static void checkSerialInterval(SortedSet<DistributedBucket> buckets) {
        Preconditions.checkNotNull(buckets);
        Preconditions.checkArgument(buckets.size() > 0, "to create an interval you should pass at least 1 bucket");

        DistributedBucket prevBucket = null;

        for (DistributedBucket currBucket : buckets) {
            if (Objects.nonNull(prevBucket) && currBucket.getBucketId() - prevBucket.getBucketId() != 1) {
                throw new IllegalArgumentException("passed buckets are not serial: " + buckets);
            }

            prevBucket = currBucket;
        }
    }

    private static class IntervalIterator implements Iterator<Integer> {

        private final int endBucket;

        private int currentBucket;

        public IntervalIterator(int startBucket, int endBucket) {
            Preconditions.checkArgument(endBucket > startBucket, "end bucket should be larger than start bucket");

            this.currentBucket = startBucket;
            this.endBucket = endBucket;
        }

        @Override
        public boolean hasNext() {
            return currentBucket < endBucket;
        }

        @Override
        public Integer next() {
            if (hasNext()) {
                return currentBucket++;
            } else {
                throw new NoSuchElementException();
            }
        }

    }

}
