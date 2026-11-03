package com.expensetracker.backend.kafka;

import com.expensetracker.backend.dto.NotificationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationKafkaProducer {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean publishNotification(NotificationRequest request) {
        if (kafkaTemplate == null) {
            System.out.println("⚠️ KafkaTemplate not available. Falling back to direct processing.");
            return false;
        }

        try {
            String payload = objectMapper.writeValueAsString(request);
            System.out.println("🚀 [KAFKA PRODUCER] Publishing event to topic '"
                    + KafkaTopicConfig.NOTIFICATION_TOPIC + "': " + payload);

            kafkaTemplate.send(KafkaTopicConfig.NOTIFICATION_TOPIC, payload);
            return true;
        } catch (Exception e) {
            System.out.println("⚠️ Kafka publish failed: " + e.getMessage() + ". Falling back to direct sync processing.");
            return false;
        }
    }
}
