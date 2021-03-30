package com.kamus.watchdog.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import com.kamus.watchdog.http.model.RepositoryDto;
import com.kamus.watchdog.http.model.RepositoryStatsDto;
import com.kamus.watchdog.http.model.StatsDto;
import org.springframework.stereotype.Service;

@Service
public class RepositoryStatsService {

    private final CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceBlockingStub commitsAnalyzerStub;

    public RepositoryStatsService(CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceBlockingStub commitsAnalyzerStub) {
        this.commitsAnalyzerStub = commitsAnalyzerStub;
    }

    public RepositoryStatsDto getStats(RepositoryDto repository) {
        long totalCommits = commitsAnalyzerStub.totalCommitsFor(toProtoRepository(repository)).getCommitsCount();
        StatsDto stats = new StatsDto(totalCommits);

        return new RepositoryStatsDto(repository, stats);
    }

    private static Repository toProtoRepository(RepositoryDto repositoryDto) {
        return Repository.newBuilder()
                       .setOwner(repositoryDto.getOwner())
                       .setName(repositoryDto.getName())
                       .build();
    }

}
