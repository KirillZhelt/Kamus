package com.kamus.watchdog.http.model;

public class StatsDto {

    private final long commitsCount;

    public StatsDto(long commitsCount) {
        this.commitsCount = commitsCount;
    }

    public long getCommitsCount() {
        return commitsCount;
    }

}
