package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.ParsedNotification;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationParserService {

    public ParsedNotification parse(String notificationText) {

        ParsedNotification parsed = new ParsedNotification();

        if (notificationText == null || notificationText.trim().isEmpty()) {
            parsed.setAmount(0.0);
            parsed.setMerchant("Unknown");
            parsed.setPaymentMethod("UNKNOWN");
            parsed.setCategory("Others");
            parsed.setTransactionType("EXPENSE");
            return parsed;
        }

        String upperText = notificationText.toUpperCase();

        // ==========================================
        // 1. Extract Amount
        // Supports: ₹100, ₹ 100, Rs. 100, Rs 100, INR 100, INR100, 100.00
        // ==========================================

        Pattern amountPattern = Pattern.compile(
                "(?:₹|Rs\\.?|INR)\\s*([\\d,]+(?:\\.\\d+)?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher amountMatcher = amountPattern.matcher(notificationText);

        if (amountMatcher.find()) {
            String cleanAmount = amountMatcher.group(1).replace(",", "");
            parsed.setAmount(Double.parseDouble(cleanAmount));
        } else {
            parsed.setAmount(0.0);
        }

        // ==========================================
        // 2. Determine Payment Method
        // ==========================================

        if (upperText.contains("UPI")) {
            parsed.setPaymentMethod("UPI");
        } else if (upperText.contains("CARD")
                || upperText.contains("CREDIT CARD")
                || upperText.contains("DEBIT CARD")) {
            parsed.setPaymentMethod("CARD");
        } else if (upperText.contains("NET BANKING")
                || upperText.contains("NETBANKING")
                || upperText.contains("IMPS")
                || upperText.contains("NEFT")
                || upperText.contains("RTGS")) {
            parsed.setPaymentMethod("NET_BANKING");
        } else if (upperText.contains("CASH")) {
            parsed.setPaymentMethod("CASH");
        } else {
            parsed.setPaymentMethod("UNKNOWN");
        }

        // ==========================================
        // 3. Autonomous Transaction Type & Flow Detection
        // ==========================================

        // A. Check for ATM Withdrawal (Bank -> Cash Wallet conversion)
        if (upperText.contains("ATM")
                || upperText.contains("CASH WDL")
                || upperText.contains("WITHDRAWN")
                || upperText.contains("WITHDRAWAL")) {

            parsed.setTransactionType("WITHDRAWAL");
            parsed.setPaymentMethod("ATM");
            parsed.setMerchant("ATM Cash Withdrawal");
            parsed.setCategory("Cash Transfer");

            System.out.println("💵 ATM Cash Withdrawal detected: ₹" + parsed.getAmount());
            return parsed;
        }

        // B. Check for Income / Credit (Salary, UPI received, Cashback, Refunds, Client payments)
        boolean isIncome = upperText.contains("CREDITED")
                || upperText.contains("RECEIVED")
                || upperText.contains("SALARY")
                || upperText.contains("REFUND")
                || upperText.contains("CASHBACK")
                || upperText.contains("DEPOSITED")
                || upperText.contains("ADDED TO")
                || upperText.contains("BONUS")
                || upperText.contains("CREDIT OF");

        if (isIncome && !upperText.contains("DEBITED")) {
            parsed.setTransactionType("INCOME");

            // Extract sender: e.g. "from Rahul", "from TechCorp", "by Amazon"
            Pattern senderPattern = Pattern.compile(
                    "(?:from|by|towards)\\s+(.+?)(?:\\s+via|\\s+using|\\s+on|\\s+ref|\\s+for|$)",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher senderMatcher = senderPattern.matcher(notificationText);
            if (senderMatcher.find()) {
                parsed.setMerchant(senderMatcher.group(1).trim());
            } else if (upperText.contains("SALARY")) {
                parsed.setMerchant("Employer / Company");
            } else if (upperText.contains("CASHBACK")) {
                parsed.setMerchant("Cashback Reward");
            } else {
                parsed.setMerchant("Income Source");
            }

            if (upperText.contains("SALARY")) {
                parsed.setCategory("Salary");
            } else if (upperText.contains("CASHBACK") || upperText.contains("REWARD")) {
                parsed.setCategory("Cashback & Rewards");
            } else if (upperText.contains("REFUND")) {
                parsed.setCategory("Refunds");
            } else {
                parsed.setCategory("Income");
            }

            System.out.println("💰 Autonomous INCOME detected: ₹" + parsed.getAmount() + " from " + parsed.getMerchant());
            return parsed;
        }

        // C. Default: Expense / Debit
        parsed.setTransactionType("EXPENSE");

        // Extract Merchant: e.g. "to Zomato", "at Starbucks", "to Uber"
        Pattern merchantPattern = Pattern.compile(
                "(?:to|at)\\s+(.+?)(?:\\s+via|\\s+using|\\s+from|\\s+on|\\s+ref|$)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher merchantMatcher = merchantPattern.matcher(notificationText);

        if (merchantMatcher.find()) {
            parsed.setMerchant(merchantMatcher.group(1).trim());
        } else {
            parsed.setMerchant("Unknown Vendor");
        }

        // Initial Category Rule
        String merchant = parsed.getMerchant().toLowerCase();
        if (merchant.contains("zomato")
                || merchant.contains("swiggy")
                || merchant.contains("domino")
                || merchant.contains("mcdonald")
                || merchant.contains("kfc")
                || merchant.contains("starbucks")
                || merchant.contains("cafe")
                || merchant.contains("restaurant")
                || merchant.contains("biryani")
                || merchant.contains("burger")) {
            parsed.setCategory("Food & Dining");
        } else if (merchant.contains("uber")
                || merchant.contains("ola")
                || merchant.contains("rapido")
                || merchant.contains("metro")
                || merchant.contains("fuel")
                || merchant.contains("petrol")) {
            parsed.setCategory("Transportation");
        } else if (merchant.contains("amazon")
                || merchant.contains("flipkart")
                || merchant.contains("myntra")
                || merchant.contains("zara")
                || merchant.contains("h&m")
                || merchant.contains("store")) {
            parsed.setCategory("Shopping");
        } else if (merchant.contains("netflix")
                || merchant.contains("spotify")
                || merchant.contains("cinema")
                || merchant.contains("pvr")
                || merchant.contains("movie")) {
            parsed.setCategory("Entertainment");
        } else if (merchant.contains("electricity")
                || merchant.contains("water")
                || merchant.contains("broadband")
                || merchant.contains("airtel")
                || merchant.contains("jio")) {
            parsed.setCategory("Bills & Utilities");
        } else {
            parsed.setCategory("Others");
        }

        // Debug Output
        System.out.println("========== PARSER RESULT ==========");
        System.out.println("Amount   : " + parsed.getAmount());
        System.out.println("Merchant : " + parsed.getMerchant());
        System.out.println("Method   : " + parsed.getPaymentMethod());
        System.out.println("Category : " + parsed.getCategory());
        System.out.println("Type     : " + parsed.getTransactionType());
        System.out.println("===================================");

        return parsed;
    }
}
