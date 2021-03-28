package com.kamus.commitsanalyzer.topology.stream;

import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.kamus.commitsanalyzer.config.KafkaConfig.DEDUPLICATED_COMMITS_TOPIC;

@Configuration
public class CommitsLoggingStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitsLoggingStream.class);

    @Autowired
    public void logCommitStream(StreamsBuilder kStreamBuilder,
                                                   KafkaProtobufSerde<RepositoryCommitMessage> commitSerde) {
        KStream<String, RepositoryCommitMessage> stream =
                kStreamBuilder.stream(DEDUPLICATED_COMMITS_TOPIC, Consumed.with(Serdes.String(), commitSerde));

        stream.foreach((key, commit) -> LOGGER.info("Consumed {}", commit));
    }

}
