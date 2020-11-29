package com.kamus.dataloader.service;

import com.google.common.collect.ImmutableList;
import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.model.Commit;
import com.kamus.dataloader.queries.GetCommitsPaginatedQuery;
import com.kamus.dataloader.queries.GetLatestCommitQuery;
import com.kamus.dataloader.util.GraphQLObservableTemplate;
import io.reactivex.rxjava3.core.Observable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GithubDataLoaderService {

    private static final int COMMITS_PER_PAGE = 100; // 100 is max and is limited by GitHub requirements

    private final GraphQLObservableTemplate githubTemplate;

    // TODO: should be stored in shared db among all data-loader instances
    private final Map<Repository, Commit> latestCommit = new HashMap<>();

    public GithubDataLoaderService(GraphQLObservableTemplate githubTemplate) {
        this.githubTemplate = githubTemplate;
    }

    public Observable<List<Commit>> getNewCommits(Repository repository) {
        Commit commit = latestCommit.get(repository);
        if (Objects.nonNull(commit)) {
            return getCommitsAfter(commit, repository);
        } else {
            return getAllCommits(repository);
        }
    }

    private Observable<List<Commit>> getCommitsAfter(Commit commit, Repository repository) {
        // TODO: can be implemented by passing until argument corresponding to commit.getCommittedTime, and handling the edge case,
        // when multiple commits may have equal (with accuracy to seconds) timestamps
        Objects.requireNonNull(commit);
        return Observable.just(Collections.emptyList());
    }

    public Observable<List<Commit>> getAllCommits(Repository repository) {
        return getLatestCommit(repository)
                .flatMap(latestCommit -> Observable.just(ImmutableList.of(latestCommit)).zipWith(getCommitsPaginated(repository,
                        latestCommit.getOid(),
                        latestCommit.getOid() + " 0",
                        COMMITS_PER_PAGE), (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toList()))
                );
    }

    private Observable<List<Commit>> getCommitsPaginated(Repository repository, String rootCommitOid, String afterCursor, int commitsPerPage) {
        return githubTemplate.queryCall(new GetCommitsPaginatedQuery(repository.getOwner(), repository.getName(), rootCommitOid, afterCursor, commitsPerPage))
                .flatMap(data -> {
                    GetCommitsPaginatedQuery.AsCommit rootCommit = (GetCommitsPaginatedQuery.AsCommit) data.repository().object();

                    List<Commit> commits = rootCommit.history().edges().stream()
                                                   .map(edge -> Commit.fromNode(edge.node()))
                                                   .collect(Collectors.toList());

                    if (rootCommit.history().pageInfo().hasNextPage()) {
                        return Observable.just(commits)
                                       .zipWith(getCommitsPaginated(repository, rootCommitOid, rootCommit.history().pageInfo().endCursor(), commitsPerPage),
                                               (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toList()));
                    } else {
                        return Observable.just(commits);
                    }
                });
    }

    public Observable<Commit> getLatestCommit(Repository repository) {
        return githubTemplate.queryCall(new GetLatestCommitQuery(repository.getOwner(), repository.getName()))
                .map(data -> Commit.fromAsCommit((GetLatestCommitQuery.AsCommit) data.repository().defaultBranchRef().target()));
    }

}
