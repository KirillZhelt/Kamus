package com.kamus.dataloader.service;

import com.google.common.collect.ImmutableList;
import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.Repository;
import com.kamus.core.db.RepositoryId;
import com.kamus.dataloader.db.model.LatestCommit;
import com.kamus.dataloader.db.repostitory.LatestCommitRepository;
import com.kamus.dataloader.model.FetchResult;
import com.kamus.dataloader.queries.GetCommitsPaginatedBeforeQuery;
import com.kamus.dataloader.queries.GetCommitsPaginatedQuery;
import com.kamus.dataloader.queries.GetCommitsPaginatedWithUntilQuery;
import com.kamus.dataloader.queries.GetLatestCommitQuery;
import com.kamus.dataloader.util.GraphQL2ProtobufConverter;
import com.kamus.dataloader.util.GraphQLObservableTemplate;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GithubDataLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(GithubDataLoaderService.class);

    private static final int COMMITS_PER_PAGE = 100; // 100 is max and is limited by GitHub requirements
    private static final int COMMITS_LIMIT = 1000;

    private final GraphQLObservableTemplate githubTemplate;
    private final LatestCommitRepository latestCommitRepository;

    public GithubDataLoaderService(GraphQLObservableTemplate githubTemplate, LatestCommitRepository latestCommitRepository) {
        this.githubTemplate = githubTemplate;
        this.latestCommitRepository = latestCommitRepository;
    }

    public Single<List<FetchResult>> getNewCommits(Set<Repository> repositories) {
        List<Single<FetchResult>> newCommitsSingles = repositories.stream()
                                                               .map(this::getNewCommits).collect(Collectors.toList());
        return Single.merge(newCommitsSingles)
                       .toList(repositories.size())
                       .subscribeOn(Schedulers.io());
    }

    public Single<FetchResult> getNewCommits(Repository repository) {
        Optional<LatestCommit> commit = latestCommitRepository.findById(new RepositoryId(repository.getOwner(), repository.getName()));
        return commit.map(latestCommit -> {
            if (Objects.nonNull(latestCommit.getNextCursor())) {
                return Single.fromObservable(getCommitsPaginatedBefore(repository, parseOid(latestCommit.getNextCursor()), latestCommit.getNextCursor(), Math.min(COMMITS_PER_PAGE, parsePosition(latestCommit.getNextCursor())),0));
            } else {
                return Single.fromObservable(getCommitsAfter(latestCommit, repository));
            }
        })
                       .orElseGet(() -> Single.fromObservable(getAllCommitsBefore(repository)));
    }

    private Observable<FetchResult> getCommitsAfter(LatestCommit commit, Repository repository) {
        Objects.requireNonNull(commit);

        return getLatestCommit(repository).flatMap(latestCommit -> {
            if (latestCommit.getSha().equals(commit.getSha())) {
                return Observable.just(new FetchResult(repository, Collections.emptyList(), null));
            } else {
                return Observable.just(ImmutableList.of(latestCommit))
                               .zipWith(getCommitsPaginatedWithUntil(repository,
                                       latestCommit.getSha(),
                                       latestCommit.getSha() + " 0",
                                       COMMITS_PER_PAGE, commit), (l, r) -> new FetchResult(repository, Stream.concat(l.stream(), r.getCommits().stream()).collect(Collectors.toList()), null)
                                );
            }
        });
    }

    private Observable<FetchResult> getCommitsPaginatedWithUntil(Repository repository, String rootCommitOid, String afterCursor, int commitsPerPage, LatestCommit untilCommit) {
        return githubTemplate.queryCall(new GetCommitsPaginatedWithUntilQuery(repository.getOwner(), repository.getName(),
                rootCommitOid, afterCursor, commitsPerPage, untilCommit.getCommitDate()))
                       .flatMap(data -> {
                           GetCommitsPaginatedWithUntilQuery.AsCommit rootCommit = (GetCommitsPaginatedWithUntilQuery.AsCommit) data.repository().object();

                           List<Commit> commits = rootCommit.history().edges().stream()
                                                          .map(edge -> GraphQL2ProtobufConverter.fromNode(repository, edge.node()))
                                                          .collect(Collectors.toList());

                           if (rootCommit.history().pageInfo().hasNextPage()) {
                               return Observable.just(commits)
                                              .zipWith(getCommitsPaginatedWithUntil(repository, rootCommitOid, rootCommit.history().pageInfo().endCursor(), commitsPerPage, untilCommit),
                                                      (l, r) -> new FetchResult(repository, Stream.concat(l.stream(), r.getCommits().stream()).collect(Collectors.toList()), null));
                           } else {
                               commits.removeIf(commit -> commit.getSha().equals(untilCommit.getSha()));
                               return Observable.just(new FetchResult(repository, commits, null));
                           }
                       });
    }

    private Observable<Commit> getLatestCommit(Repository repository) {
        return githubTemplate.queryCall(new GetLatestCommitQuery(repository.getOwner(), repository.getName()))
                .map(data -> GraphQL2ProtobufConverter.fromAsCommit(repository, (GetLatestCommitQuery.AsCommit) data.repository().defaultBranchRef().target()));
    }

    private Observable<GetLatestCommitQuery.Data> getLatestCommitBefore(Repository repository) {
        return githubTemplate.queryCall(new GetLatestCommitQuery(repository.getOwner(), repository.getName()));
    }

    private Observable<FetchResult> getAllCommitsBefore(Repository repository) {
        return getLatestCommitBefore(repository)
                       .flatMap(data -> {
                           GetLatestCommitQuery.AsCommit latestCommit = (GetLatestCommitQuery.AsCommit) data.repository().defaultBranchRef().target();

                           if (latestCommit.history().totalCount() < 2) {
                               return Observable.just(new FetchResult(repository, ImmutableList.of(GraphQL2ProtobufConverter.fromAsCommit(repository, latestCommit)), null));
                           }

                           String beforeCursor = String.format("%s %s", latestCommit.oid(), latestCommit.history().totalCount());
                           int commitsPerPage = Math.min(COMMITS_PER_PAGE, latestCommit.history().totalCount());

                           return getCommitsPaginatedBefore(repository, latestCommit.oid().toString(), beforeCursor, commitsPerPage, 0);
                       }).flatMap(fr -> {
                           Collections.reverse(fr.getCommits());
                           return Observable.just(new FetchResult(fr.getRepository(), fr.getCommits(), fr.getNextCursor().orElse(null)));
                });
    }

    private Observable<FetchResult> getCommitsPaginatedBefore(Repository repository, String rootCommitOid, String beforeCursor, int commitsPerPage, int commitsLoaded) {
        return githubTemplate.queryCall(new GetCommitsPaginatedBeforeQuery(repository.getOwner(), repository.getName(), rootCommitOid, beforeCursor, commitsPerPage))
                .flatMap(data -> {
                    GetCommitsPaginatedBeforeQuery.AsCommit rootCommit = (GetCommitsPaginatedBeforeQuery.AsCommit) data.repository().object();

                    List<Commit> commits = rootCommit.history().edges().stream()
                                                   .map(edge -> GraphQL2ProtobufConverter.fromNode(repository, edge.node()))
                                                   .collect(Collectors.toList());

                    logger.info("Loaded {} commits for repository {}. Total {}", commits.size(), repository, commitsLoaded + commits.size());

                    int newCommitsPerPage = Math.min(COMMITS_PER_PAGE, parsePosition(rootCommit.history().pageInfo().startCursor()));

                    if (rootCommit.history().pageInfo().hasPreviousPage()
                                && commitsLoaded + commits.size() + newCommitsPerPage <= COMMITS_LIMIT) {
                        return Observable.just(commits)
                                       .zipWith(getCommitsPaginatedBefore(repository, rootCommitOid, rootCommit.history().pageInfo().startCursor(), newCommitsPerPage, commitsLoaded + commits.size()),
                                               (l, r) -> new FetchResult(repository, Stream.concat(l.stream(), r.getCommits().stream()).collect(Collectors.toList()), r.getNextCursor().orElse(null)));
                    } else if (!rootCommit.history().pageInfo().hasPreviousPage()) {
                        return Observable.just(new FetchResult(repository, commits, null));
                    } else {
                        return Observable.just(new FetchResult(repository, commits, rootCommit.history().pageInfo().startCursor()));
                    }
                });
    }

    private static int parsePosition(String cursor) {
        return Integer.parseInt(cursor.split(" ")[1]);
    }

    private static String parseOid(String cursor) {
        return cursor.split(" ")[0];
    }

}
