package com.kamus.loaderconfig.db.model;

import com.google.common.base.Preconditions;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class TrackedRepository {

    @EmbeddedId
    private RepositoryId id;

    // reflects how often the repository is updated
    // used for partitioning the repositories into loader configurations
    // metric value must be in [1; 100]
    // the higher the value the more often repository is updated
    private int metric;
    private boolean tracked;

    protected TrackedRepository() {

    }

    public TrackedRepository(RepositoryId id, int metric, boolean tracked) {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(metric >= 1 && metric <= 100, "metric should be in [1; 100]");

        this.id = id;
        this.metric = metric;
        this.tracked = tracked;
    }

    public RepositoryId getId() {
        return id;
    }

    public void setId(RepositoryId id) {
        this.id = id;
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
}
