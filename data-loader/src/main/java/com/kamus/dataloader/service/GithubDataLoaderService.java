package com.kamus.dataloader.service;

import com.google.common.collect.ImmutableList;
import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.db.model.LatestCommit;
import com.kamus.dataloader.db.model.RepositoryId;
import com.kamus.dataloader.db.repostitory.LatestCommitRepository;
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
    private final LatestCommitRepository latestCommitRepository;

    public GithubDataLoaderService(GraphQLObservableTemplate githubTemplate, LatestCommitRepository latestCommitRepository) {
        this.githubTemplate = githubTemplate;
        this.latestCommitRepository = latestCommitRepository;
    }

    public Single<List<Commit>> getNewCommits(Repository repository) {
        Optional<LatestCommit> commit = latestCommitRepository.findById(new RepositoryId(repository.getOwner(), repository.getName()));
        return commit.map(latestCommit -> Single.fromObservable(getCommitsAfter(latestCommit, repository)))
                       .orElseGet(() -> Single.fromObservable(getAllCommits(repository)));
    }

    private Observable<List<Commit>> getCommitsAfter(LatestCommit commit, Repository repository) {
        Objects.requireNonNull(commit);

        return getLatestCommit(repository).flatMap(latestCommit -> {
            if (latestCommit.getSha().equals(commit.getSha())) {
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

    private Observable<List<Commit>> getCommitsPaginatedWithUntil(Repository repository, String rootCommitOid, String afterCursor, int commitsPerPage, LatestCommit untilCommit) {
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
                                                      (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toList()));
                           } else {
                               commits.removeIf(commit -> commit.getSha().equals(untilCommit.getSha()));
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
                                                   .map(edge -> GraphQL2ProtobufConverter.fromNode(repository, edge.node()))
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
                .map(data -> GraphQL2ProtobufConverter.fromAsCommit(repository, (GetLatestCommitQuery.AsCommit) data.repository().defaultBranchRef().target()));
    }

}
