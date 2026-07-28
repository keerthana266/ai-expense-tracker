package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AiCategorizationResponse;
import org.springframework.stereotype.Service;

@Service
public class AiCategorizationService {

    public AiCategorizationResponse categorize(String notificationText) {

        String text = notificationText.toLowerCase();

        if (text.contains("zomato") || text.contains("swiggy")) {
            return new AiCategorizationResponse(
                    "Food",
                    0.98,
                    "Detected food delivery merchant."
            );
        }

        if (text.contains("uber") || text.contains("ola")) {
            return new AiCategorizationResponse(
                    "Travel",
                    0.96,
                    "Detected ride booking service."
            );
        }

        if (text.contains("amazon") || text.contains("flipkart")) {
            return new AiCategorizationResponse(
                    "Shopping",
                    0.95,
                    "Detected e-commerce merchant."
            );
        }

        return new AiCategorizationResponse(
                "Others",
                0.60,
                "Unknown merchant."
        );
    }
}