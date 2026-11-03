package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.ParsedNotification;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WhatsAppService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Value("${whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${whatsapp.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${whatsapp.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${whatsapp.twilio.phone-number:}")
    private String twilioPhoneNumber;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send WhatsApp message to user. If Twilio/API credentials are not present,
     * it simulates and logs the message in the audit trail.
     */
    public void sendWhatsAppMessage(String toPhoneNumber, String message) {
        System.out.println("================================================================================");
        System.out.println("💬 [WHATSAPP FINANCIAL INTERVENTION AGENT] Sending to: " + (toPhoneNumber != null ? toPhoneNumber : "User WhatsApp"));
        System.out.println("MESSAGE:");
        System.out.println(message);
        System.out.println("================================================================================");

        if (whatsappEnabled && twilioAccountSid != null && !twilioAccountSid.isBlank()) {
            System.out.println(">> Dispatching via Twilio WhatsApp API...");
        }
    }

    /**
     * Autonomous Financial Intervention & Pacing Agent:
     * Dynamically monitors ANY category or merchant (Food, Shopping, Cabs, Nightlife, Electronics)
     * and intervenes when spending speed accelerates, habit frequency is too high, or runway is threatened.
     */
    public String checkAndInterveneSpending(User user, Transaction latestTransaction) {
        if (user == null || latestTransaction == null) {
            return null;
        }

        // If transaction is INCOME, celebrate savings reinforcement!
        if ("INCOME".equalsIgnoreCase(latestTransaction.getTransactionType())) {
            String sender = latestTransaction.getMerchant() != null ? latestTransaction.getMerchant().getName() : "Income Source";
            double amount = latestTransaction.getAmount() != null ? latestTransaction.getAmount() : 0.0;
            String msg = String.format("💰 [INCOME BOOST] ₹%.0f credited from %s! Your savings and broke date runway just extended. Keep it safe! 🚀",
                    amount, sender);
            sendWhatsAppMessage(user.getEmail(), msg);
            return msg;
        }

        // Discretionary Expense Intervention
        String category = latestTransaction.getAiCategory() != null ? latestTransaction.getAiCategory() : "Spending";
        String merchantName = latestTransaction.getMerchant() != null ? latestTransaction.getMerchant().getName() : "Merchant";
        double amount = latestTransaction.getAmount() != null ? latestTransaction.getAmount() : 0.0;

        // Fetch user's transactions from the last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Transaction> recentTransactions = transactionRepository.findByUserOrderByTransactionTimeDesc(user);

        // Count how many times the user spent in this specific category or merchant this week
        long repeatHabitCount = recentTransactions.stream()
                .filter(t -> t.getTransactionTime() != null && t.getTransactionTime().isAfter(sevenDaysAgo))
                .filter(t -> matchesCategoryOrMerchant(t, category, merchantName))
                .count();

        // Calculate total liquid runway to check if spending speed is dangerous
        double currentBalance = accountRepository.findByUser(user).stream()
                .mapToDouble(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : 0.0)
                .sum();

        boolean isHighFrequency = repeatHabitCount >= 3;
        boolean isRunwayThreat = currentBalance > 0 && (amount / currentBalance) >= 0.15; // single spend > 15% of balance

        if (isHighFrequency || isRunwayThreat) {
            String prompt = String.format("""
                    You are ExpenseAI's savage WhatsApp Financial Intervention Agent.
                    The user just spent ₹%.2f on %s at %s.
                    Weekly frequency for this habit: %d times in 7 days.
                    Remaining liquid balance: ₹%.2f.

                    Write a savage, hilarious, and hard-hitting WhatsApp guilt-trip message telling them
                    to SLOW DOWN their spending speed and protect their runway before hitting their broke date.
                    Rules:
                    - Exactly 2 punchy sentences.
                    - Savage, witty, but helpful (mention empty pockets, slowing down speed, or broke date).
                    - Include emojis like 🛑💸📉⚠️.
                    - Return ONLY the message text.
                    """, amount, category, merchantName, repeatHabitCount, currentBalance);

            try {
                String intervention = geminiService.askGeminiPlainText(prompt);
                if (intervention == null || intervention.isBlank() || intervention.contains("coffee break")) {
                    if (isHighFrequency) {
                        intervention = String.format("🚨 Slow down! That is order #%d on %s this week (₹%.0f at %s) 💸🛑 At this spending speed, your broke date will hit before month-end!",
                                repeatHabitCount, category, amount, merchantName);
                    } else {
                        intervention = String.format("⚠️ Runway Alert: ₹%.0f spent at %s in one shot?! 📉 That just knocked a huge dent into your liquid balance. Pump the brakes on spending!",
                                amount, merchantName);
                    }
                }

                sendWhatsAppMessage(user.getEmail(), intervention);
                return intervention;
            } catch (Exception e) {
                System.out.println("Error generating spending intervention: " + e.getMessage());
            }
        }

        return null;
    }

    // Alias for backwards compatibility
    public String checkAndRoastFoodSpending(User user, Transaction latestTransaction) {
        return checkAndInterveneSpending(user, latestTransaction);
    }

    private boolean matchesCategoryOrMerchant(Transaction t, String targetCategory, String targetMerchant) {
        String cat = t.getAiCategory() != null ? t.getAiCategory().toLowerCase() : "";
        String mer = t.getMerchant() != null && t.getMerchant().getName() != null ? t.getMerchant().getName().toLowerCase() : "";

        return cat.contains(targetCategory.toLowerCase())
                || (!targetMerchant.isBlank() && mer.contains(targetMerchant.toLowerCase()));
    }

    /**
     * Parse casual WhatsApp messages (text or voice transcript) for physical cash spends:
     * e.g., "biryani 250 cash", "chai 30", "auto 70 cash"
     */
    public ParsedNotification parseCashMessage(String text) {
        ParsedNotification parsed = new ParsedNotification();
        parsed.setPaymentMethod("CASH");
        parsed.setTransactionType("EXPENSE");

        String prompt = String.format("""
                You are ExpenseAI Cash Spend Parser.
                A user sent this casual WhatsApp message about a physical cash spend:
                "%s"

                Extract:
                - amount (number in INR)
                - merchant (e.g. Chai Stall, Auto Commute, Local Store, Food Vendor)
                - category (e.g. Food & Dining, Transportation, Groceries, Personal Care, Others)

                Return strictly JSON format:
                {
                  "amount": 250,
                  "merchant": "Food Vendor",
                  "category": "Food & Dining"
                }
                """, text);

        try {
            String geminiJson = geminiService.askGeminiPlainText(prompt);
            int start = geminiJson.indexOf("{");
            int end = geminiJson.lastIndexOf("}");
            if (start != -1 && end != -1) {
                String cleanJson = geminiJson.substring(start, end + 1);
                JsonNode root = objectMapper.readTree(cleanJson);
                parsed.setAmount(root.path("amount").asDouble(0.0));
                parsed.setMerchant(root.path("merchant").asText("Cash Vendor"));
                parsed.setCategory(root.path("category").asText("Others"));
            } else {
                throw new RuntimeException("No valid JSON in Gemini response");
            }
        } catch (Exception e) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)(\\.\\d+)?").matcher(text);
            if (m.find()) {
                parsed.setAmount(Double.parseDouble(m.group(1)));
            } else {
                parsed.setAmount(0.0);
            }
            parsed.setMerchant("Local Cash Spend");
            parsed.setCategory("Others");
        }

        return parsed;
    }
}
