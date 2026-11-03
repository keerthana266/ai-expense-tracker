package com.expensetracker.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class RoastService {

    @Autowired
    private GeminiService geminiService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public String generateRoast() {
        return "✨ Fresh start! Zero spending recorded. Your wallet is at absolute peace. Add an expense to wake up the roast bot! 🤖";
    }

    public String generateRoast(double totalExpense, String topMerchant, String topCategory) {
        if (totalExpense <= 0) {
            return "✨ Fresh start! Zero spending recorded. Your wallet is at absolute peace. Add an expense to wake up the roast bot! 🤖";
        }

        System.out.println("Generating AI roast for: ₹" + totalExpense + ", merchant: " + topMerchant);

        String prompt = """
                You are a funny AI financial coach with brutal honesty.

                Based on the user's spending:
                Total Expense: ₹%.2f
                Top Merchant: %s
                Top Category: %s

                Generate ONE funny roast about their spending habit (e.g. Biryani/food cravings, impulsive online shopping).
                Rules:
                - Maximum 2 sentences.
                - Funny, sassy, guilt-tripping but not offensive.
                - Add one suitable emoji.
                - Return ONLY the roast string.
                """
                .formatted(totalExpense, topMerchant, topCategory);

        Future<String> future = executor.submit(() -> geminiService.askGemini(prompt));

        try {
            String roast = future.get(5, TimeUnit.SECONDS);
            if (roast == null || roast.isBlank()) {
                return fallbackRoast(totalExpense, topMerchant, topCategory);
            }
            System.out.println("Roast generated: " + roast);
            return roast;
        } catch (Exception e) {
            System.out.println("Gemini roast fallback due to: " + e.getMessage());
            return fallbackRoast(totalExpense, topMerchant, topCategory);
        }
    }

    private String fallbackRoast(double totalExpense, String topMerchant, String topCategory) {
        String merchantLower = (topMerchant != null ? topMerchant : "").toLowerCase();
        String categoryLower = (topCategory != null ? topCategory : "").toLowerCase();

        if (merchantLower.contains("biryani") || categoryLower.contains("food") || merchantLower.contains("swiggy") || merchantLower.contains("zomato")) {
            return "Biryani 4 times this week? 🫢 Your wallet is crying more than your diet!";
        }

        if (merchantLower.contains("amazon") || categoryLower.contains("shopping")) {
            return "Amazon delivery driver knows your address better than your relatives! Stop impulse buying. 📦";
        }

        if (topMerchant != null && !topMerchant.isBlank() && !topMerchant.equalsIgnoreCase("Unknown")) {
            return topMerchant + " seems to know your wallet better than you do! 💸";
        }

        return "You're spending money faster than your phone battery drains! Pace your spending. 💸";
    }
}
