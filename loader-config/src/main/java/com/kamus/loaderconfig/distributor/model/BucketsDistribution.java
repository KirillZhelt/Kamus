package com.kamus.loaderconfig.distributor.model;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BucketsDistribution {

    private final AssignedBucketsInterval assignedBuckets;

    private final Set<Integer> bucketsAdded;
    private final Set<Integer> bucketsRemoved;

    public static BucketsDistribution newBucketsAddedDistribution(AssignedBucketsInterval assignedBuckets,
                                                                  Set<Integer> bucketsAdded) {
        return new BucketsDistribution(assignedBuckets, bucketsAdded, Collections.emptySet());
    }

    public static BucketsDistribution newBucketsRemovedDistribution(AssignedBucketsInterval assignedBuckets,
                                                                    Set<Integer> bucketsRemoved) {
        return new BucketsDistribution(assignedBuckets, Collections.emptySet(), bucketsRemoved);
    }

    public static BucketsDistribution newOnInitBucketsDistribution(AssignedBucketsInterval assignedBuckets) {
        return new BucketsDistribution(assignedBuckets, Collections.emptySet(), Collections.emptySet());
    }

    public static BucketsDistribution newBucketsDistribution(AssignedBucketsInterval assignedBuckets,
                                                             Set<Integer> bucketsAdded,
                                                             Set<Integer> bucketsRemoved) {
        return new BucketsDistribution(assignedBuckets, bucketsAdded, bucketsRemoved);
    }

    public BucketsDistribution(AssignedBucketsInterval assignedBuckets,
                               Set<Integer> bucketsAdded, Set<Integer> bucketsRemoved) {
        Preconditions.checkNotNull(assignedBuckets);
        Preconditions.checkNotNull(bucketsAdded);
        Preconditions.checkNotNull(bucketsRemoved);

        this.assignedBuckets = assignedBuckets;
        this.bucketsAdded = new HashSet<>(bucketsAdded);
        this.bucketsRemoved = new HashSet<>(bucketsRemoved);
    }

    public boolean hasBucketsAdded() {
        return !bucketsAdded.isEmpty();
    }

    public boolean hasBucketsRemoved() {
        return !bucketsRemoved.isEmpty();
    }

    public AssignedBucketsInterval getAssignedBuckets() {
        return assignedBuckets;
    }

    public Set<Integer> getBucketsAdded() {
        return bucketsAdded;
    }

    public Set<Integer> getBucketsRemoved() {
        return bucketsRemoved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketsDistribution that = (BucketsDistribution) o;
        return Objects.equals(assignedBuckets, that.assignedBuckets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignedBuckets);
    }

}
