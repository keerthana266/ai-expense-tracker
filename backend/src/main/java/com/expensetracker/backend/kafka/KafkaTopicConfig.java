package com.expensetracker.backend.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfig {

    public static final String NOTIFICATION_TOPIC = "expense.notifications.raw";
    public static final String CASH_TOPIC = "expense.cash.raw";

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cashTopic() {
        return TopicBuilder.name(CASH_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
