package com.expensetracker.backend.service;

import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ExpenseRoastService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private GeminiService geminiService;

    public String generateRoast() {

        List<Transaction> transactions = transactionRepository.findAll();

        if (transactions.isEmpty()) {
            return "No expenses found. Your wallet is safe 😄";
        }

        // Calculate total spending
        double totalSpent = transactions.stream()
                .mapToDouble(t -> t.getAmount() == null ? 0.0 : t.getAmount())
                .sum();

        // Find highest transaction
        Transaction highestTransaction = transactions.stream()
                .max(Comparator.comparing(
                        t -> t.getAmount() == null ? 0.0 : t.getAmount()))
                .orElse(null);

        String merchant = "Unknown";

        if (highestTransaction != null && highestTransaction.getMerchant() != null) {
            merchant = highestTransaction.getMerchant().getName();
        }

        String prompt = """
You are a funny financial advisor.

Generate a short funny roast based on the user's expenses.

User Details:
- Total Spending: ₹%.2f
- Highest Spending Merchant: %s

Rules:
1. Maximum 40 words.
2. Funny but not offensive.
3. Don't use emojis except one at the end.
4. Return ONLY the roast text.
""".formatted(totalSpent, merchant);

        try {
            return geminiService.askGemini(prompt);
        } catch (Exception e) {
            return "You and " + merchant + " are best friends now 😂";
        }
    }
}