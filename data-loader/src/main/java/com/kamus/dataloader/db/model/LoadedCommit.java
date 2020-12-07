package com.kamus.dataloader.db.model;

import com.google.common.base.Preconditions;

import javax.persistence.*;

@Entity
public class LoadedCommit {

    @ManyToOne
    private LatestCommit latestCommit;

    @Id
    private String sha;

    @Embedded
    private CommitInfo commitInfo;

    protected LoadedCommit() {

    }

    public LoadedCommit(LatestCommit latestCommit, String sha, CommitInfo commitInfo) {
        Preconditions.checkNotNull(latestCommit);
        Preconditions.checkNotNull(sha);
        Preconditions.checkNotNull(commitInfo);

        this.sha = sha;
        this.latestCommit = latestCommit;
        this.commitInfo = commitInfo;
    }

    public LatestCommit getLatestCommit() {
        return latestCommit;
    }

    public void setLatestCommit(LatestCommit latestCommit) {
        this.latestCommit = latestCommit;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public CommitInfo getCommitInfo() {
        return commitInfo;
    }

    public void setCommitInfo(CommitInfo commitInfo) {
        this.commitInfo = commitInfo;
    }
}
