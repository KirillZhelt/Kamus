package com.kamus.watchdog.kafka;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.SendTo;

@Configuration
@EnableBinding(KafkaStreamsProcessor.class)
public class UtilStream {

    @StreamListener("input")
    @SendTo("output")
    public KStream<?, String> process(KStream<?, String> input) {
        return input.mapValues((k, v) -> v.substring(0, 7));
    }

}
