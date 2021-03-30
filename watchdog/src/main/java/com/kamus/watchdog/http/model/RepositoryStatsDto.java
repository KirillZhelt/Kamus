package com.kamus.watchdog.http.model;

import com.google.common.base.Preconditions;

public class RepositoryStatsDto {

    private final RepositoryDto repository;
    private final StatsDto stats;

    public RepositoryStatsDto(RepositoryDto repository, StatsDto stats) {
        Preconditions.checkNotNull(repository);

        this.repository = repository;
        this.stats = stats;
    }

    public RepositoryDto getRepository() {
        return repository;
    }

    public StatsDto getStats() {
        return stats;
    }

}
