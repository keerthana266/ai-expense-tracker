package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ParsedNotification;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.UserRepository;
import com.expensetracker.backend.service.TransactionService;
import com.expensetracker.backend.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@CrossOrigin("*")
public class WhatsAppController {

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Webhook to receive incoming WhatsApp messages (Voice transcripts or text).
     * Solves the Liquid Cash loophole by enabling 2-second natural language logging.
     * e.g., "chai sutta 30", "biryani 250 cash", "auto 60"
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleIncomingWhatsApp(
            @RequestBody(required = false) Map<String, Object> bodyPayload,
            @RequestParam(value = "Body", required = false) String twilioBody,
            @RequestParam(value = "From", required = false) String twilioFrom
    ) {
        String messageText = "";
        String sender = "User";

        if (twilioBody != null && !twilioBody.isBlank()) {
            messageText = twilioBody;
            sender = twilioFrom != null ? twilioFrom : "WhatsApp User";
        } else if (bodyPayload != null) {
            messageText = (String) bodyPayload.getOrDefault("message", bodyPayload.getOrDefault("Body", ""));
            sender = (String) bodyPayload.getOrDefault("from", bodyPayload.getOrDefault("From", "WhatsApp User"));
        }

        System.out.println("📥 Incoming WhatsApp Message from " + sender + ": " + messageText);

        // Find user by security context or fallback to first registered user
        User user = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                user = userRepository.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}

        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }

        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "No active user account found.");
            return ResponseEntity.badRequest().body(error);
        }

        // Parse cash transaction with Gemini
        ParsedNotification parsed = whatsAppService.parseCashMessage(messageText);

        // Save transaction to Cash Wallet
        transactionService.saveTransaction(parsed, "WhatsApp: " + messageText);

        // Get updated cash wallet balance
        double updatedCashBalance = 0.0;
        Account cashWallet = accountRepository.findByUser(user).stream()
                .filter(a -> "CASH".equalsIgnoreCase(a.getAccountType()) || "Cash Wallet".equalsIgnoreCase(a.getAccountName()))
                .findFirst()
                .orElse(null);
        if (cashWallet != null && cashWallet.getCurrentBalance() != null) {
            updatedCashBalance = cashWallet.getCurrentBalance();
        }

        String replyMessage = String.format("✅ Logged ₹%.0f for %s (%s) from your Cash Wallet. Cash remaining: ₹%.0f 💵",
                parsed.getAmount(), parsed.getMerchant(), parsed.getCategory(), updatedCashBalance);

        whatsAppService.sendWhatsAppMessage(sender, replyMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reply", replyMessage);
        response.put("parsed", parsed);
        response.put("cashBalance", updatedCashBalance);

        return ResponseEntity.ok(response);
    }

    /**
     * Test trigger for AI Roast WhatsApp guilt trip (e.g. 4th Biryani of the week).
     */
    @PostMapping("/test-roast")
    public ResponseEntity<Map<String, Object>> testWhatsAppRoast(@RequestParam(defaultValue = "Biryani") String item) {
        String testMessage = "🚨 Bro, that's your 4th " + item + " of the week! 🍛💀 " +
                "Your wallet is already on life support and your broke date is coming faster than your order. Close the app!";
        whatsAppService.sendWhatsAppMessage("+919876543210", testMessage);

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "sent");
        resp.put("roast", testMessage);
        return ResponseEntity.ok(resp);
    }
}
