package com.expensetracker.backend.kafka;

import com.expensetracker.backend.dto.NotificationRequest;
import com.expensetracker.backend.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationKafkaConsumer {

    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = KafkaTopicConfig.NOTIFICATION_TOPIC, groupId = "expense-ai-consumer-group")
    public void consumeNotificationEvent(String messagePayload) {
        System.out.println("==================================================");
        System.out.println("⚡ [KAFKA CONSUMER] High-Throughput Event Received!");
        System.out.println("Payload: " + messagePayload);
        System.out.println("==================================================");

        try {
            NotificationRequest request = objectMapper.readValue(messagePayload, NotificationRequest.class);
            notificationService.processNotification(request);
            System.out.println("✅ [KAFKA CONSUMER] Event successfully processed and categorized by Gemini.");
        } catch (Exception e) {
            System.err.println("❌ [KAFKA CONSUMER] Error processing notification: " + e.getMessage());
        }
    }
}
