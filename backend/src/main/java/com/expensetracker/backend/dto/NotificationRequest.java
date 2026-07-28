package com.expensetracker.backend.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private String notificationText;

    private String packageName;

    private Long timestamp;
}