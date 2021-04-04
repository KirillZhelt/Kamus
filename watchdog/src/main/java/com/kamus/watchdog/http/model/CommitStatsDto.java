package com.kamus.watchdog.http.model;

public class CommitStatsDto {

    private final long commitsCount;

    private final long commitsCount30Days;
    private final long commitsCountLastWeek;
    private final long commitsCountToday;

    public CommitStatsDto(long commitsCount, long commitsCount30Days, long commitsCountLastWeek, long commitsCountToday) {
        this.commitsCount = commitsCount;
        this.commitsCount30Days = commitsCount30Days;
        this.commitsCountLastWeek = commitsCountLastWeek;
        this.commitsCountToday = commitsCountToday;
    }

    public long getCommitsCount() {
        return commitsCount;
    }

    public long getCommitsCount30Days() {
        return commitsCount30Days;
    }

    public long getCommitsCountLastWeek() {
        return commitsCountLastWeek;
    }

    public long getCommitsCountToday() {
        return commitsCountToday;
    }

}
