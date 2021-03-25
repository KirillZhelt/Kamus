package com.kamus.commitsanalyzer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.kamus.commitsanalyzer.config.KafkaConfig.COMMITS_SHAS_TOPIC;
import static com.kamus.commitsanalyzer.config.KafkaConfig.DEDUPLICATED_COMMITS_TOPIC;

@Configuration
public class KafkaStreamTopics {

    @Bean
    public NewTopic commitsShasTopic() {
        return new NewTopic(COMMITS_SHAS_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic dedupCommitsTopic() {
        return new NewTopic(DEDUPLICATED_COMMITS_TOPIC, 3, (short) 1);
    }

}
