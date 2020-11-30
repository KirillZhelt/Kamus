package com.kamus.dataloader.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.model.CommitFootprint;
import com.kamus.dataloader.queries.GetCommitsPaginatedQuery;
import com.kamus.dataloader.queries.GetCommitsPaginatedWithUntilQuery;
import com.kamus.dataloader.queries.GetLatestCommitQuery;
import com.kamus.dataloader.util.GraphQL2ProtobufConverter;
import com.kamus.dataloader.util.GraphQLObservableTemplate;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GithubDataLoaderService {

    private static final int COMMITS_PER_PAGE = 100; // 100 is max and is limited by GitHub requirements

    private final GraphQLObservableTemplate githubTemplate;

    // TODO: should be stored in shared db among all data-loader instances
    private final Map<Repository, CommitFootprint> latestCommit = ImmutableMap.of(
            Repository.newBuilder().setOwner("KirillZhelt").setName("MayMayMay").build(),
            new CommitFootprint("2019-12-03T19:26:16Z", "ce9113b84518e30b8f5cb670c526fdbc2c7bd41c")
    );

    public GithubDataLoaderService(GraphQLObservableTemplate githubTemplate) {
        this.githubTemplate = githubTemplate;
    }

    public Single<List<Commit>> getNewCommits(Repository repository) {
        CommitFootprint commit = latestCommit.get(repository);
        if (Objects.nonNull(commit)) {
            return Single.fromObservable(getCommitsAfter(commit, repository));
        } else {
            return Single.fromObservable(getAllCommits(repository));
        }
    }

    private Observable<List<Commit>> getCommitsAfter(CommitFootprint commit, Repository repository) {
        Objects.requireNonNull(commit);

        return getLatestCommit(repository).flatMap(latestCommit -> {
            if (latestCommit.equals(commit)) {
                return Observable.just(Collections.emptyList());
            } else {
                return Observable.just(ImmutableList.of(latestCommit))
                               .zipWith(getCommitsPaginatedWithUntil(repository,
                                       latestCommit.getSha(),
                                       latestCommit.getSha() + " 0",
                                       COMMITS_PER_PAGE, commit), (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toList())
                                );
            }
        });
    }

    private Observable<List<Commit>> getCommitsPaginatedWithUntil(Repository repository, String rootCommitOid, String afterCursor, int commitsPerPage, CommitFootprint untilCommit) {
        return githubTemplate.queryCall(new GetCommitsPaginatedWithUntilQuery(repository.getOwner(), repository.getName(),
                rootCommitOid, afterCursor, commitsPerPage, untilCommit.getCommitDate()))
                       .flatMap(data -> {
                           GetCommitsPaginatedWithUntilQuery.AsCommit rootCommit = (GetCommitsPaginatedWithUntilQuery.AsCommit) data.repository().object();

                           List<Commit> commits = rootCommit.history().edges().stream()
                                                          .map(edge -> GraphQL2ProtobufConverter.fromNode(edge.node()))
                                                          .collect(Collectors.toList());

                           if (rootCommit.history().pageInfo().hasNextPage()) {
                               return Observable.just(commits)
                                              .zipWith(getCommitsPaginatedWithUntil(repository, rootCommitOid, rootCommit.history().pageInfo().endCursor(), commitsPerPage, untilCommit),
                                                      (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toList()));
                           } else {
                               commits.removeIf(commit -> commit.equals(untilCommit));
                               return Observable.just(commits);
                           }
                       });
    }

    private Observable<List<Commit>> getAllCommits(Repository repository) {
        return getLatestCommit(repository)
                .flatMap(latestCommit -> Observable.just(ImmutableList.of(latestCommit)).zipWith(getCommitsPaginated(repository,
                        latestCommit.getSha(),
                        latestCommit.getSha() + " 0",
                        COMMITS_PER_PAGE), (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toList()))
                );
    }

    private Observable<List<Commit>> getCommitsPaginated(Repository repository, String rootCommitOid, String afterCursor, int commitsPerPage) {
        return githubTemplate.queryCall(new GetCommitsPaginatedQuery(repository.getOwner(), repository.getName(), rootCommitOid, afterCursor, commitsPerPage))
                .flatMap(data -> {
                    GetCommitsPaginatedQuery.AsCommit rootCommit = (GetCommitsPaginatedQuery.AsCommit) data.repository().object();

                    List<Commit> commits = rootCommit.history().edges().stream()
                                                   .map(edge -> GraphQL2ProtobufConverter.fromNode(edge.node()))
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

    private Observable<Commit> getLatestCommit(Repository repository) {
        return githubTemplate.queryCall(new GetLatestCommitQuery(repository.getOwner(), repository.getName()))
                .map(data -> GraphQL2ProtobufConverter.fromAsCommit((GetLatestCommitQuery.AsCommit) data.repository().defaultBranchRef().target()));
    }

}
