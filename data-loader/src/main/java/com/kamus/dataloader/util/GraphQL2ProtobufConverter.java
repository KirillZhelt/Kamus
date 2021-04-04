package com.kamus.dataloader.util;

import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.CommitStats;
import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.queries.GetCommitsPaginatedBeforeQuery;
import com.kamus.dataloader.queries.GetCommitsPaginatedQuery;
import com.kamus.dataloader.queries.GetCommitsPaginatedWithUntilQuery;
import com.kamus.dataloader.queries.GetLatestCommitQuery;

public final class GraphQL2ProtobufConverter {

    private GraphQL2ProtobufConverter() {
    }

    public static Commit fromAsCommit(Repository repository, GetLatestCommitQuery.AsCommit asCommit) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(asCommit.additions())
                                    .setDeletions(asCommit.deletions())
                                    .setChangedFiles(asCommit.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(asCommit.author().name())
                       .setAuthorEmail(asCommit.author().email())
                       .setRepository(repository)
                       .setStats(stats)
                       .setCommitDate((String) asCommit.committedDate())
                       .setSha((String) asCommit.oid())
                       .build();
    }

    public static Commit fromNode(Repository repository, GetCommitsPaginatedQuery.Node node) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(node.additions())
                                    .setDeletions(node.deletions())
                                    .setChangedFiles(node.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(node.author().name())
                       .setAuthorEmail(node.author().email())
                       .setStats(stats)
                       .setRepository(repository)
                       .setCommitDate((String) node.committedDate())
                       .setSha((String) node.oid())
                       .build();
    }

    public static Commit fromNode(Repository repository, GetCommitsPaginatedWithUntilQuery.Node node) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(node.additions())
                                    .setDeletions(node.deletions())
                                    .setChangedFiles(node.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(node.author().name())
                       .setAuthorEmail(node.author().email())
                       .setStats(stats)
                       .setRepository(repository)
                       .setCommitDate((String) node.committedDate())
                       .setSha((String) node.oid())
                       .build();
    }

    public static Commit fromNode(Repository repository, GetCommitsPaginatedBeforeQuery.Node node) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(node.additions())
                                    .setDeletions(node.deletions())
                                    .setChangedFiles(node.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(node.author().name())
                       .setAuthorEmail(node.author().email())
                       .setStats(stats)
                       .setRepository(repository)
                       .setCommitDate((String) node.committedDate())
                       .setSha((String) node.oid())
                       .build();
    }

}
