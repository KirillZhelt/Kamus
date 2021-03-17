package com.kamus.loaderconfig.db.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Comparator;

@Entity
public class DistributedBucket {

    @Id
    private Integer bucketId;

    @NotNull
    private String loaderId;

    protected DistributedBucket() {}

    public DistributedBucket(Integer bucketId, String loaderId) {
        Preconditions.checkNotNull(bucketId);
        Preconditions.checkNotNull(loaderId);

        this.bucketId = bucketId;
        this.loaderId = loaderId;
    }

    public Integer getBucketId() {
        return bucketId;
    }

    public void setBucketId(Integer bucketId) {
        this.bucketId = bucketId;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public static class IdAscendingComparator implements Comparator<DistributedBucket> {

        @Override
        public int compare(DistributedBucket o1, DistributedBucket o2) {
            return o1.bucketId.compareTo(o2.bucketId);
        }

    }

    public interface LoaderIdProjection {

        String getLoaderId();

    }

}
