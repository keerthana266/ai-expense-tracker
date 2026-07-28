package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.NotificationRequest;
import com.expensetracker.backend.dto.ParsedNotification;
import com.expensetracker.backend.entity.Notification;
import com.expensetracker.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationParserService parserService;

    @Autowired
    private TransactionService transactionService;

    public String processNotification(NotificationRequest request) {

        // Save raw notification
        Notification notification = new Notification();

        notification.setNotificationText(request.getNotificationText());
        notification.setPackageName(request.getPackageName());
        notification.setTimestamp(request.getTimestamp());

        notificationRepository.save(notification);

        // Parse notification
        ParsedNotification parsed = parserService.parse(request.getNotificationText());
        transactionService.saveTransaction(parsed);
        
        System.out.println("========== PARSED NOTIFICATION ==========");
        System.out.println("Merchant : " + parsed.getMerchant());
        System.out.println("Amount   : " + parsed.getAmount());
        System.out.println("Method   : " + parsed.getPaymentMethod());
        System.out.println("Category : " + parsed.getCategory());
        System.out.println("=========================================");

        return "Notification saved and parsed successfully!";
    }
}