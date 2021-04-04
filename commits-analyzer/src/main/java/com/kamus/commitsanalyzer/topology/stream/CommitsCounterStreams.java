package com.kamus.commitsanalyzer.topology.stream;

import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CommitsCounterStreams {

    public static final String COMMITS_COUNT_PER_REPOSITORY_STORE = "CommitsCountPerRepositoryStore";
    public static final String COMMITS_COUNT_PER_REPOSITORY_STORE_31_DAYS = "CommitsCountPerRepositoryStore31Days";

    @Autowired
    public void commitsCountPerRepository(KStream<String, RepositoryCommitMessage> dedupCommitsStream,
                                          KafkaProtobufSerde<RepositoryCommitMessage> commitSerde,
                                          KafkaProtobufSerde<Repository> repositorySerde) {
        dedupCommitsStream.groupBy((k, v) -> v.getCommit().getRepository(), Grouped.with(repositorySerde, commitSerde))
                .count(Materialized.as(COMMITS_COUNT_PER_REPOSITORY_STORE));
    }

    @Autowired
    public void commitsCountPerRepositoryFor31DaysAggregatedByDay(KStream<String, RepositoryCommitMessage> dedupCommitsStream,
                                          KafkaProtobufSerde<RepositoryCommitMessage> commitSerde,
                                          KafkaProtobufSerde<Repository> repositorySerde) {
        Materialized<Repository, Long, WindowStore<Bytes, byte[]>> materialized =
                Materialized.<Repository, Long, WindowStore<Bytes, byte[]>>as(COMMITS_COUNT_PER_REPOSITORY_STORE_31_DAYS)
                        .withRetention(Duration.ofDays(64));

        dedupCommitsStream.groupBy((k, v) -> v.getCommit().getRepository(), Grouped.with(repositorySerde, commitSerde))
                .windowedBy(TimeWindows.of(Duration.ofDays(1)).grace(Duration.ofDays(32)))
                .count(materialized);
    }

}
