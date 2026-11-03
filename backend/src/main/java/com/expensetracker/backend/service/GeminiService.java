package com.expensetracker.backend.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // ============================================================
    // TRANSACTION CATEGORIZATION
    // ============================================================

    public String categorizeTransaction(String notificationText) {

        System.out.println("========== GEMINI CATEGORIZATION ==========");
        System.out.println("Sending transaction to Gemini...");

        try {

            if (apiKey == null || apiKey.isBlank()) {
                System.out.println("Gemini API key is missing.");
                return """
                {
                  "merchant": "Unknown",
                  "amount": 0,
                  "category": "Others",
                  "confidence": 0,
                  "reasoning": "Gemini API key is missing."
                }
                """;
            }

            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            String prompt = """
                    You are an AI Expense Categorizer for a personal finance application.

                    Analyse the payment notification below.

                    Extract:
                    - merchant
                    - amount
                    - category
                    - confidence
                    - reasoning

                    Use categories such as:
                    Food & Dining
                    Shopping
                    Transportation
                    Bills & Utilities
                    Entertainment
                    Healthcare
                    Education
                    Travel
                    Groceries
                    Others

                    Return ONLY valid JSON.

                    Required format:

                    {
                      "merchant": "Zomato",
                      "amount": 350,
                      "category": "Food & Dining",
                      "confidence": 0.98,
                      "reasoning": "Food delivery payment"
                    }

                    Notification:
                    """ + notificationText;

            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-flash-latest",
                            prompt,
                            null
                    );

            String result = response.text();

            System.out.println("Gemini categorization response received.");
            System.out.println(result);
            System.out.println("============================================");

            return result;

        } catch (Exception e) {

            System.out.println("========== GEMINI CATEGORIZATION ERROR ==========");
            System.out.println(
                    e.getClass().getSimpleName() + ": " + e.getMessage()
            );
            e.printStackTrace();
            System.out.println("==================================================");

            return """
            {
              "merchant": "Unknown",
              "amount": 0,
              "category": "Others",
              "confidence": 0,
              "reasoning": "Unable to analyse notification using Gemini."
            }
            """;
        }
    }


    // ============================================================
    // GENERAL GEMINI REQUEST
    // Used for AI Insights / Roast Mode / other AI features
    // ============================================================

    public String askGemini(String prompt) {

        System.out.println("========== GEMINI REQUEST ==========");
        System.out.println("Sending request to Gemini...");

        try {

            // ----------------------------------------------------
            // Check API key
            // ----------------------------------------------------

            if (apiKey == null || apiKey.isBlank()) {

                System.out.println("Gemini API key is missing.");

                return """
                {
                  "insights": [
                    {
                      "icon": "🤖",
                      "title": "AI unavailable",
                      "message": "Gemini API key is missing. Please check application.properties."
                    },
                    {
                      "icon": "📊",
                      "title": "Analytics available",
                      "message": "Your spending analytics are still available."
                    },
                    {
                      "icon": "💰",
                      "title": "Keep tracking",
                      "message": "Continue recording transactions to improve your financial insights."
                    }
                  ]
                }
                """;
            }


            // ----------------------------------------------------
            // Create Gemini client
            // ----------------------------------------------------

            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();


            // ----------------------------------------------------
            // Send request
            // ----------------------------------------------------

            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-flash-latest",
                            prompt,
                            null
                    );


            // ----------------------------------------------------
            // Read response
            // ----------------------------------------------------

            String result = response.text();

            System.out.println("Gemini response received.");
            System.out.println("Gemini response:");
            System.out.println(result);
            System.out.println("===================================");

            return result;


        } catch (Exception e) {

            // ----------------------------------------------------
            // IMPORTANT:
            // Print the actual Gemini error.
            // This is what we need if the API call fails.
            // ----------------------------------------------------

            System.out.println("========== GEMINI REQUEST ERROR ==========");

            System.out.println(
                    "Error Type: "
                            + e.getClass().getName()
            );

            System.out.println(
                    "Error Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.out.println("==========================================");


            // ----------------------------------------------------
            // Fallback response
            // ----------------------------------------------------

            return """
            {
              "insights": [
                {
                  "icon": "🤖",
                  "title": "SpendWise AI",
                  "message": "AI insights are temporarily unavailable. Please try again."
                },
                {
                  "icon": "📊",
                  "title": "Analytics available",
                  "message": "Your spending analytics are still available above."
                },
                {
                  "icon": "💰",
                  "title": "Keep tracking",
                  "message": "Continue recording transactions to improve your financial insights."
                }
              ]
            }
            """;
        }
    }
    public String askGeminiPlainText(String prompt) {

        System.out.println("========== GEMINI PLAIN TEXT REQUEST ==========");
        System.out.println("Sending plain-text request to Gemini...");

        try {

                if (apiKey == null || apiKey.isBlank()) {
                return "🤖 SpendWise AI is unavailable because the Gemini API key is missing.";
                }

                Client client = Client.builder()
                        .apiKey(apiKey)
                        .build();

                GenerateContentResponse response =
                        client.models.generateContent(
                                "gemini-flash-latest",
                                prompt,
                                null
                        );

                String result = response.text();

                if (result == null || result.isBlank()) {
                return "🤖 Your wallet escaped the roast this time. Try again!";
                }

                System.out.println("Gemini plain-text response:");
                System.out.println(result);

                return result.trim();

        } catch (Exception e) {

                System.out.println("========== GEMINI PLAIN TEXT ERROR ==========");
                System.out.println(
                        e.getClass().getName() + ": " + e.getMessage()
                );

                /*
                * Gemini free-tier quota exhausted.
                * Return a safe fallback so the application
                * continues working instead of showing an error.
                */
                if (e.getMessage() != null &&
                        e.getMessage().contains("429")) {

                System.out.println(
                        "Gemini quota exceeded. Using SpendWise fallback roast."
                );

                    throw e;
                }

                e.printStackTrace();

                return "🤖 SpendWise tried to roast your spending, but the AI is taking a coffee break.";
        }
        }
}