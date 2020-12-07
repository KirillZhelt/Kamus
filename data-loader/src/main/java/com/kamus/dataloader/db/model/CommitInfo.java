package com.kamus.dataloader.db.model;

import com.google.common.base.Preconditions;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
public class CommitInfo implements Serializable {

    private String repositoryName;
    private String repositoryOwner;

    private int additions;
    private int deletions;
    private int changedFiles;

    private String authorName;
    private String authorEmail;
    private LocalDateTime commitDate;

    protected CommitInfo() {

    }

    public CommitInfo(String repositoryName, String repositoryOwner, int additions, int deletions, int changedFiles,
                      String authorName, String authorEmail, LocalDateTime commitDate) {
        Preconditions.checkNotNull(repositoryName);
        Preconditions.checkNotNull(repositoryOwner);
        Preconditions.checkNotNull(authorName);
        Preconditions.checkNotNull(authorEmail);
        Preconditions.checkNotNull(commitDate);

        this.repositoryName = repositoryName;
        this.repositoryOwner = repositoryOwner;
        this.additions = additions;
        this.deletions = deletions;
        this.changedFiles = changedFiles;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.commitDate = commitDate;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryOwner() {
        return repositoryOwner;
    }

    public void setRepositoryOwner(String repositoryOwner) {
        this.repositoryOwner = repositoryOwner;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(int changedFiles) {
        this.changedFiles = changedFiles;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public LocalDateTime getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
    }
}
