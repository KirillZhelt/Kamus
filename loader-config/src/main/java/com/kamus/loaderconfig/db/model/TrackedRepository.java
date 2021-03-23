package com.kamus.loaderconfig.db.model;

import com.google.common.base.Preconditions;
import com.kamus.core.db.RepositoryId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "bucketId"))
public class TrackedRepository {

    @EmbeddedId
    private RepositoryId id;

    private int bucketId;

    // reflects how often the repository is updated
    // used for partitioning the repositories into loader configurations
    // metric value must be in [1; 100]
    // the higher the value the more often repository is updated
    private int metric;
    private boolean tracked;

    protected TrackedRepository() {

    }

    public TrackedRepository(RepositoryId id, int bucketId, int metric, boolean tracked) {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(metric >= 1 && metric <= 100, "metric should be in [1; 100]");
        Preconditions.checkArgument(bucketId >= 0, "bucketId shouldn't be negative");

        this.id = id;
        this.bucketId = bucketId;

        this.metric = metric;
        this.tracked = tracked;
    }

    public RepositoryId getId() {
        return id;
    }

    public void setId(RepositoryId id) {
        this.id = id;
    }

    public int getBucketId() {
        return bucketId;
    }

    public void setBucketId(int bucketId) {
        this.bucketId = bucketId;
    }

    public int getMetric() {
        return metric;
    }

    public void setMetric(int metric) {
        this.metric = metric;
    }

    public boolean isTracked() {
        return tracked;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    public interface RepositoryIdView {
        RepositoryId getId();
    }
}
