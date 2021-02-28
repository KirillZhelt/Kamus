package com.kamus.loaderconfig.distributor.model;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class BucketId {

    private final int bucketId;

    public BucketId(int bucketId) {
        Preconditions.checkArgument(bucketId >= 0, "Bucket id should be non-negative");

        this.bucketId = bucketId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketId bucketId1 = (BucketId) o;
        return bucketId == bucketId1.bucketId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketId);
    }

    @Override
    public String toString() {
        return "BucketId{" +
                       "bucketId=" + bucketId +
                       '}';
    }
}
