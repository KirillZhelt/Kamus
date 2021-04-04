package com.kamus.dataloader.model;

import com.google.common.base.Preconditions;
import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.Repository;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class FetchResult {

    private final List<Commit> commits;
    private final Repository repository;

    private final String nextCursor;

    public FetchResult(Repository repository, List<Commit> commits, @Nullable String nextCursor) {
        Preconditions.checkNotNull(repository);
        Preconditions.checkNotNull(commits);

        this.repository = repository;
        this.commits = commits;
        this.nextCursor = nextCursor;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public Optional<String> getNextCursor() {
        return Optional.ofNullable(nextCursor);
    }

    public Repository getRepository() {
        return repository;
    }
}
