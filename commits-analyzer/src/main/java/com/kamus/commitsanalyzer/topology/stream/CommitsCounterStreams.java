package com.kamus.commitsanalyzer.topology.stream;

import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommitsCounterStreams {

    public static final String COMMITS_COUNT_PER_REPOSITORY_STORE = "CommitsCountPerRepositoryStore";

    @Autowired
    public void commitsCountPerRepository(KStream<String, RepositoryCommitMessage> dedupCommitsStream,
                                          KafkaProtobufSerde<RepositoryCommitMessage> commitSerde,
                                          KafkaProtobufSerde<Repository> repositorySerde) {
        dedupCommitsStream.groupBy((k, v) -> v.getCommit().getRepository(), Grouped.with(repositorySerde, commitSerde))
                .count(Materialized.as(COMMITS_COUNT_PER_REPOSITORY_STORE));
    }

}
