package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.NotificationRequest;
import com.expensetracker.backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public String receiveNotification(@RequestBody NotificationRequest request) {
        return notificationService.processNotification(request);
    }
}