package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.ParsedNotification;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationParserService {

    public ParsedNotification parse(String notificationText) {

        ParsedNotification parsed = new ParsedNotification();

        // Extract amount
        Pattern amountPattern = Pattern.compile("₹\\s?(\\d+(\\.\\d+)?)");
        Matcher amountMatcher = amountPattern.matcher(notificationText);

        if (amountMatcher.find()) {
            parsed.setAmount(Double.parseDouble(amountMatcher.group(1)));
        }

        // Extract merchant
        Pattern merchantPattern = Pattern.compile("to\\s+([A-Za-z0-9& ]+?)\\s+via");
        Matcher merchantMatcher = merchantPattern.matcher(notificationText);

        if (merchantMatcher.find()) {
            parsed.setMerchant(merchantMatcher.group(1).trim());
        }

        // Payment method
        if (notificationText.toUpperCase().contains("UPI")) {
            parsed.setPaymentMethod("UPI");
        } else {
            parsed.setPaymentMethod("UNKNOWN");
        }

        // Category (temporary rule-based)
        String merchant = parsed.getMerchant();

        if (merchant != null) {

            String m = merchant.toLowerCase();

            if (m.contains("zomato") || m.contains("swiggy") || m.contains("domino")) {
                parsed.setCategory("Food");
            } else if (m.contains("uber") || m.contains("ola")) {
                parsed.setCategory("Travel");
            } else if (m.contains("amazon") || m.contains("flipkart")) {
                parsed.setCategory("Shopping");
            } else {
                parsed.setCategory("Others");
            }
        }

        return parsed;
    }
}