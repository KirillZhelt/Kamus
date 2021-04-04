package com.kamus.commitsanalyzer.config;

import com.kamus.commitsanalyzer.kafka.CommitsAnalyzerTimeExtractor;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import io.grpc.Server;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.state.HostInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    public static final String COMMITS_TOPIC = "kamus.commits.new";
    public static final String COMMITS_SHAS_TOPIC = "kamus.commits.shas";
    public static final String DEDUPLICATED_COMMITS_TOPIC = "kamus.dedup.commits";

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${grpc.commits-analyzer-service.port}")
    private int grpcServerPort;

    @Bean
    public HostInfo hostInfo() throws UnknownHostException {
        return new HostInfo(InetAddress.getLocalHost().getHostAddress(), grpcServerPort);
    }

    @Bean
    public String applicationId() {
        return "kamus.commits.commits.analyzer";
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig(ApplicationContext context) {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, CommitsAnalyzerTimeExtractor.class.getName());

        HostInfo hostInfo = context.getBean(HostInfo.class);
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, hostInfo.host() + ":" + hostInfo.port());

        props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new KafkaStreamsConfiguration(props);
    }

}
