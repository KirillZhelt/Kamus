package com.kamus.commitsanalyzer.topology.stream;

import com.kamus.common.grpcjava.Commit;
import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.kamus.commitsanalyzer.config.KafkaConfig.COMMITS_SHAS_TOPIC;
import static com.kamus.commitsanalyzer.config.KafkaConfig.DEDUPLICATED_COMMITS_TOPIC;

@Configuration
public class CommitsLoggingStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitsLoggingStream.class);

    @Bean
    public KStream<String, String> logCommitStream(StreamsBuilder kStreamBuilder,
                                                   KafkaProtobufSerde<RepositoryCommitMessage> commitSerde) {
        KStream<String, RepositoryCommitMessage> stream =
                kStreamBuilder.stream(DEDUPLICATED_COMMITS_TOPIC, Consumed.with(Serdes.String(), commitSerde));

        KStream<String, String> shasStream = stream
                                                     .mapValues(commitMessage -> {
                                                         LOGGER.info("Consumed {}", commitMessage);
                                                         return commitMessage.getCommit();
                                                     })
                                                     .mapValues(Commit::getSha);
        shasStream.to(COMMITS_SHAS_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        return shasStream;
    }

}
