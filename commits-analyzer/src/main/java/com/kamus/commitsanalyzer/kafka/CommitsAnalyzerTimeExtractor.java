package com.kamus.commitsanalyzer.kafka;

import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.Instant;

public class CommitsAnalyzerTimeExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        if (record.value() instanceof RepositoryCommitMessage) {
            return Instant.parse(((RepositoryCommitMessage) record.value()).getCommit().getCommitDate()).toEpochMilli();
        }

        return record.timestamp();
    }
}
