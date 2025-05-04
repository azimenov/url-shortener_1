package org.example.urlshortenerwrite.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.url-events}")
    private String urlEventsTopic;

    @Bean
    public NewTopic urlEventsTopic() {
        return TopicBuilder.name(urlEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}