package com.kamus.commitsanalyzer.config;

import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaStreamsSerdesConfig {

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public KafkaProtobufSerde<RepositoryCommitMessage> commitSerde() {
        KafkaProtobufSerde<RepositoryCommitMessage> serde = new KafkaProtobufSerde<>();
        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        serdeConfig.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, RepositoryCommitMessage.class.getName());
        serde.configure(serdeConfig, false);
        return serde;
    }

}
