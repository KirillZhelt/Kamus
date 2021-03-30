package com.kamus.core.kafka.grpc.streams.sharding.internal;

import com.google.common.base.Preconditions;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

// ATM it is a registry for SINGLETON KafkaStreams object
public class KafkaStreamsSingletonRegistry implements StreamsBuilderFactoryBean.Listener {

    private final String applicationId;

    private boolean initialized = false;
    private KafkaStreams streams;

    public KafkaStreamsSingletonRegistry(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        streamsBuilderFactoryBean.addListener(this);

        this.applicationId = streamsBuilderFactoryBean.getStreamsConfiguration()
                                     .getProperty(StreamsConfig.APPLICATION_ID_CONFIG);
    }

    public KafkaStreams getKafkaStreams() {
        if (!initialized) {
            throw new IllegalStateException("KafkaStreams haven't been initialized yet");
        }

        return streams;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public void streamsAdded(String id, KafkaStreams streams) {
        if (initialized) {
            throw new IllegalArgumentException("KafkaStreams was already initialized");
        }

        this.streams = streams;
        initialized = true;
    }

    @Override
    public void streamsRemoved(String id, KafkaStreams streams) {
        Preconditions.checkArgument(this.streams == streams);

        streams = null;
    }
}
