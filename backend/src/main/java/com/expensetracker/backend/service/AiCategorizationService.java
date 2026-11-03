package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AiCategorizationResponse;
import com.expensetracker.backend.dto.GeminiTransactionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiCategorizationService {

    @Autowired
    private GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiCategorizationResponse categorize(String notificationText) {

        try {

            String jsonResponse =
                    geminiService.categorizeTransaction(notificationText);

            System.out.println("==================================");
            System.out.println("Gemini Raw Response:");
            System.out.println(jsonResponse);
            System.out.println("==================================");

            // Remove markdown if Gemini returns ```json ... ```
            jsonResponse = jsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            GeminiTransactionResponse response =
                    objectMapper.readValue(
                            jsonResponse,
                            GeminiTransactionResponse.class
                    );

            // Normalize Gemini category
            String category = normalizeCategory(
                    response.getCategory()
            );

            return new AiCategorizationResponse(
                    category,
                    response.getConfidence(),
                    response.getReasoning()
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new AiCategorizationResponse(
                    "Others",
                    0.50,
                    "Gemini parsing failed"
            );
        }
    }

    /**
     * Converts Gemini's category response into one
     * of the application's standard categories.
     */
    private String normalizeCategory(String category) {

        if (category == null || category.trim().isEmpty()) {
            return "Others";
        }

        String normalized =
                category.trim().toLowerCase();

        switch (normalized) {

            case "food":
            case "food & dining":
            case "food and dining":
            case "dining":
            case "restaurant":
            case "restaurants":
            case "groceries":
            case "grocery":
                return "Food & Dining";

            case "shopping":
            case "shop":
            case "clothing":
            case "online shopping":
                return "Shopping";

            case "travel":
            case "transport":
            case "transportation":
            case "fuel":
            case "uber":
            case "ola":
                return "Travel";

            case "bills":
            case "bill":
            case "utilities":
            case "utility":
            case "electricity":
            case "internet":
            case "mobile":
                return "Bills & Utilities";

            case "entertainment":
            case "movies":
            case "movie":
            case "games":
            case "gaming":
                return "Entertainment";

            case "health":
            case "medical":
            case "medicine":
            case "pharmacy":
                return "Health";

            case "education":
            case "course":
            case "courses":
            case "books":
                return "Education";

            case "salary":
            case "income":
            case "freelance":
                return "Income";

            default:
                return "Others";
        }
    }
}