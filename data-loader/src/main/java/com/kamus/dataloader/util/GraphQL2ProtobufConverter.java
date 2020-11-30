package com.kamus.dataloader.util;

import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.CommitStats;
import com.kamus.dataloader.queries.GetCommitsPaginatedQuery;
import com.kamus.dataloader.queries.GetCommitsPaginatedWithUntilQuery;
import com.kamus.dataloader.queries.GetLatestCommitQuery;

public final class GraphQL2ProtobufConverter {

    private GraphQL2ProtobufConverter() {
    }

    public static Commit fromAsCommit(GetLatestCommitQuery.AsCommit asCommit) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(asCommit.additions())
                                    .setDeletions(asCommit.deletions())
                                    .setChangedFiles(asCommit.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(asCommit.author().name())
                       .setAuthorEmail(asCommit.author().email())
                       .setStats(stats)
                       .setCommitDate((String) asCommit.committedDate())
                       .setSha((String) asCommit.oid())
                       .build();
    }

    public static Commit fromNode(GetCommitsPaginatedQuery.Node node) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(node.additions())
                                    .setDeletions(node.deletions())
                                    .setChangedFiles(node.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(node.author().name())
                       .setAuthorEmail(node.author().email())
                       .setStats(stats)
                       .setCommitDate((String) node.committedDate())
                       .setSha((String) node.oid())
                       .build();
    }

    public static Commit fromNode(GetCommitsPaginatedWithUntilQuery.Node node) {
        CommitStats stats = CommitStats.newBuilder()
                                    .setAdditions(node.additions())
                                    .setDeletions(node.deletions())
                                    .setChangedFiles(node.changedFiles())
                                    .build();

        return Commit.newBuilder()
                       .setAuthorName(node.author().name())
                       .setAuthorEmail(node.author().email())
                       .setStats(stats)
                       .setCommitDate((String) node.committedDate())
                       .setSha((String) node.oid())
                       .build();
    }

}
