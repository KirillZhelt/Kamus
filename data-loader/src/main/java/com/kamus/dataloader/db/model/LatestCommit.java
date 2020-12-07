package com.kamus.dataloader.db.model;

import com.google.common.base.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class LatestCommit {

    @EmbeddedId
    private RepositoryId id;

    private String sha;

    private LocalDateTime commitDate;

    @OneToMany(mappedBy = "latestCommit", cascade = { CascadeType.ALL })
    private Set<LoadedCommit> loadedCommits;

    protected LatestCommit() {

    }

    public LatestCommit(RepositoryId id, String sha, LocalDateTime commitDate) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(sha);
        Preconditions.checkNotNull(commitDate);

        this.id = id;
        this.sha = sha;
        this.commitDate = commitDate;
    }

    public RepositoryId getId() {
        return id;
    }

    public void setId(RepositoryId id) {
        this.id = id;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public LocalDateTime getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
    }

    public Set<LoadedCommit> getLoadedCommits() {
        return loadedCommits;
    }

    public void setLoadedCommits(Set<LoadedCommit> loadedCommits) {
        this.loadedCommits = loadedCommits;
    }

}
