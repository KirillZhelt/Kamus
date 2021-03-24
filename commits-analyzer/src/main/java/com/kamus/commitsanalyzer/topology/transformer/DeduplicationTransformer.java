package com.kamus.commitsanalyzer.topology.transformer;

import com.google.common.base.Preconditions;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

import java.util.Objects;

public class DeduplicationTransformer<K, V, E> implements ValueTransformerWithKey<K, V, V> {

    private final long leftDurationMs;
    private final long rightDurationMs;
    private final String storeName;

    private final KeyValueMapper<K, V, E> idExtractor;

    private ProcessorContext processorContext;
    private WindowStore<E, Long> eventIdStore;

    public DeduplicationTransformer(long maintainDurationPerEventInMs, KeyValueMapper<K, V, E> idExtractor,
                                    String storeName) {
        Preconditions.checkArgument(maintainDurationPerEventInMs > 0, "maintain duration per event must be >= 1");

        this.leftDurationMs = maintainDurationPerEventInMs / 2;
        this.rightDurationMs = maintainDurationPerEventInMs - leftDurationMs;
        this.storeName = storeName;

        this.idExtractor = idExtractor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.processorContext = context;
        eventIdStore = (WindowStore<E, Long>) context.getStateStore(storeName);
    }

    @Override
    public V transform(K readOnlyKey, V value) {
        E eventId = idExtractor.apply(readOnlyKey, value);
        if (Objects.isNull(eventId)) {
            return value;
        } else {
            V output;
            if (isDuplicate(eventId)) {
                output = null;
                updateTimestampOfExistingEventToPreventExpiry(eventId, processorContext.timestamp());
            } else {
                output = value;
                rememberNewEvent(eventId, processorContext.timestamp());
            }

            return output;
        }
    }

    private void updateTimestampOfExistingEventToPreventExpiry(E eventId, long timestamp) {
        eventIdStore.put(eventId, timestamp, timestamp);
    }

    private void rememberNewEvent(E eventId, long timestamp) {
        eventIdStore.put(eventId, timestamp, timestamp);
    }

    private boolean isDuplicate(E eventId) {
        long eventTime = processorContext.timestamp();
        WindowStoreIterator<Long> timeIterator = eventIdStore.fetch(
                eventId,
                eventTime - leftDurationMs,
                eventTime + rightDurationMs
        );

        boolean isDuplicate = timeIterator.hasNext();
        timeIterator.close();
        return isDuplicate;
    }

    @Override
    public void close() {

    }
}
