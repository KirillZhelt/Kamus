package com.kamus.dataloader.model;

import java.util.Objects;

public class CommitFootprint {

    private final String commitDate;
    private final String sha;

    public CommitFootprint(String commitDate, String sha) {
        Objects.requireNonNull(commitDate);
        Objects.requireNonNull(sha);

        this.commitDate = commitDate;
        this.sha = sha;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public String getSha() {
        return sha;
    }

}
