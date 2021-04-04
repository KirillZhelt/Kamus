package com.kamus.watchdog.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import com.kamus.loaderconfig.grpcjava.CommitsCountFor31DaysResponse;
import com.kamus.watchdog.http.model.RepositoryDto;
import com.kamus.watchdog.http.model.RepositoryStatsDto;
import com.kamus.watchdog.http.model.CommitStatsDto;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

@Service
public class RepositoryStatsService {

    private final CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceBlockingStub commitsAnalyzerStub;

    public RepositoryStatsService(CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceBlockingStub commitsAnalyzerStub) {
        this.commitsAnalyzerStub = commitsAnalyzerStub;
    }

    public RepositoryStatsDto getStats(RepositoryDto repository) {
        long totalCommits = commitsAnalyzerStub.commitsCountPerRepositoryStore(toProtoRepository(repository)).getCommitsCount();

        CommitStatsDto stats;

        try {
            CommitsCountFor31DaysResponse response =
                    commitsAnalyzerStub.commitsCountPerRepositoryFor31DaysAggregatedByDay(toProtoRepository(repository));

            stats = new CommitStatsDto(totalCommits,
                    commitsCount30Days(response),
                    commitsCountLastWeek(response),
                    commitsCountToday(response));
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().equals(Status.NOT_FOUND)) {
                stats = new CommitStatsDto(totalCommits,
                        0,
                        0,
                        0);
            } else {
                throw ex;
            }
        }

        return new RepositoryStatsDto(repository, stats);
    }

    private long commitsCount30Days(CommitsCountFor31DaysResponse response) {
        return response.getCommitsCountForDayMap()
                       .values()
                       .stream()
                       .mapToLong(it -> it)
                       .sum();
    }

    private long commitsCountLastWeek(CommitsCountFor31DaysResponse response) {
        return response.getCommitsCountForDayMap()
                       .entrySet()
                       .stream()
                       .filter(it -> Instant.parse(it.getKey()).isAfter(Instant.now().minus(8, ChronoUnit.DAYS)))
                       .mapToLong(Map.Entry::getValue)
                       .sum();
    }

    private long commitsCountToday(CommitsCountFor31DaysResponse response) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Instant todayInstant = Instant.ofEpochMilli(calendar.getTimeInMillis());

        return response.getCommitsCountForDayMap().getOrDefault(todayInstant.toString(), 0L);
    }

    private static Repository toProtoRepository(RepositoryDto repositoryDto) {
        return Repository.newBuilder()
                       .setOwner(repositoryDto.getOwner())
                       .setName(repositoryDto.getName())
                       .build();
    }

}
