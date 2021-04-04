package com.kamus.commitsanalyzer.topology.transformer;

import com.google.common.base.Preconditions;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

import java.util.Objects;

// deduplicates records by id in the given window interval (time is processing time not record time)
public class DeduplicationTransformer<K, V, E> implements ValueTransformerWithKey<K, V, V> {

    private final long leftDurationMs;
    private final long rightDurationMs;
    private final String storeName;

    private final KeyValueMapper<K, V, E> idExtractor;

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
                updateTimestampOfExistingEventToPreventExpiry(eventId, System.currentTimeMillis());
            } else {
                output = value;
                rememberNewEvent(eventId, System.currentTimeMillis());
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
        long eventTime = System.currentTimeMillis();
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
