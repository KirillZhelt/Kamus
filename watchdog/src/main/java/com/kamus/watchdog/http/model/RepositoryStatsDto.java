package com.kamus.watchdog.http.model;

import com.google.common.base.Preconditions;

public class RepositoryStatsDto {

    private final RepositoryDto repository;
    private final CommitStatsDto stats;

    public RepositoryStatsDto(RepositoryDto repository, CommitStatsDto stats) {
        Preconditions.checkNotNull(repository);

        this.repository = repository;
        this.stats = stats;
    }

    public RepositoryDto getRepository() {
        return repository;
    }

    public CommitStatsDto getStats() {
        return stats;
    }

}
