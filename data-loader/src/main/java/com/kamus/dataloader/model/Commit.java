package com.kamus.dataloader.model;

import com.kamus.dataloader.queries.GetCommitsPaginatedQuery;
import com.kamus.dataloader.queries.GetLatestCommitQuery;

import java.util.Objects;

public class Commit {

    private final String authorName;
    private final String authorEmail;
    private final int additions;
    private final int deletions;
    private final int changedFiles;
    private final String committedDate;
    private final String oid;

    public Commit(String authorName, String authorEmail, int additions, int deletions, int changedFiles, String committedDate, String oid) {
        Objects.requireNonNull(authorName);
        Objects.requireNonNull(authorEmail);
        Objects.requireNonNull(committedDate);
        Objects.requireNonNull(oid);

        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.additions = additions;
        this.deletions = deletions;
        this.changedFiles = changedFiles;
        this.committedDate = committedDate;
        this.oid = oid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public int getAdditions() {
        return additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public String getCommittedDate() {
        return committedDate;
    }

    public String getOid() {
        return oid;
    }

    public static Commit fromAsCommit(GetLatestCommitQuery.AsCommit asCommit) {
        return new Commit(asCommit.author().name(), asCommit.author().email(),
                asCommit.additions(), asCommit.deletions(), asCommit.changedFiles(),
                (String) asCommit.committedDate(), (String) asCommit.oid());
    }

    public static Commit fromNode(GetCommitsPaginatedQuery.Node node) {
        return new Commit(node.author().name(), node.author().email(),
                node.additions(), node.deletions(), node.changedFiles(),
                (String) node.committedDate(), (String) node.oid());
    }
}
