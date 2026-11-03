package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.NotificationRequest;
import com.expensetracker.backend.kafka.NotificationKafkaProducer;
import com.expensetracker.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired(required = false)
    private NotificationKafkaProducer kafkaProducer;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public String receiveNotification(@RequestBody NotificationRequest request) {

        System.out.println("==================================================");
        System.out.println("📲 [NOTIFICATION CONTROLLER HIT] Payment detected!");
        System.out.println("App/Package : " + request.getPackageName());
        System.out.println("Raw Alert   : " + request.getNotificationText());
        System.out.println("==================================================");

        // Feature 1 + Tech Stack Flex: Route event through Kafka for high-throughput stream processing
        if (kafkaProducer != null) {
            boolean published = kafkaProducer.publishNotification(request);
            if (published) {
                return "⚡ Notification queued to Kafka topic 'expense.notifications.raw' for real-time AI processing!";
            }
        }

        // Resilient fallback: Process synchronously if Kafka cluster is offline
        System.out.println("ℹ️ Processing synchronously with Gemini AI...");
        return notificationService.processNotification(request);
    }
}
