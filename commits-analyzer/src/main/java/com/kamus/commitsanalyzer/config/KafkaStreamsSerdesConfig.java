package com.kamus.commitsanalyzer.config;

import com.kamus.common.grpcjava.Repository;
import com.kamus.core.kafka.grpc.streams.sharding.internal.ShardingKeysRegistry;
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

    @Bean
    public KafkaProtobufSerde<Repository> repositorySerde() {
        KafkaProtobufSerde<Repository> serde = new KafkaProtobufSerde<>();
        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        serdeConfig.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, Repository.class.getName());
        serde.configure(serdeConfig, false);
        return serde;
    }

    @Bean
    public ShardingKeysRegistry shardingKeysRegistry() {
        // that is used for gRPC sharding "load" balancer
        ShardingKeysRegistry shardingKeysRegistry = new ShardingKeysRegistry();

        registerShardingKeys(shardingKeysRegistry);

        return shardingKeysRegistry;
    }

    private void registerShardingKeys(ShardingKeysRegistry shardingKeysRegistry) {
        shardingKeysRegistry.registerShardingKey(new ShardingKeysRegistry.Entry<>(Repository.class, repositorySerde().serializer()));
    }

}
