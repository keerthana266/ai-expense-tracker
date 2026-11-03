package com.expensetracker.backend.config;

import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.Budget;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.BudgetRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            System.out.println("==================================================");
            System.out.println("🚀 [EXPENSE AI] Initializing Clean Working Model...");
            System.out.println("==================================================");

            // 1. Ensure default demo user exists
            String email = "keerthana@gmail.com";
            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

            if (user == null) {
                user = userRepository.findAll().stream().findFirst().orElse(null);
            }

            if (user == null) {
                user = User.builder()
                        .fullName("Keerthana")
                        .email(email)
                        .password(passwordEncoder.encode("password123"))
                        .createdAt(LocalDateTime.now())
                        .enabled(true)
                        .role("USER")
                        .build();
                user = userRepository.save(user);
                System.out.println("✅ Default user created: " + user.getEmail() + " (Password: password123)");
            } else {
                // Ensure password is password123
                user.setPassword(passwordEncoder.encode("password123"));
                userRepository.save(user);
                System.out.println("✅ User confirmed: " + user.getEmail() + " (Password: password123)");
            }

            // 2. Clear old transactions so starting model is set to 0
            try {
                long txCount = transactionRepository.count();
                if (txCount > 0) {
                    transactionRepository.deleteAll();
                    System.out.println("🧹 Cleared " + txCount + " old transactions to reset starting state to 0.");
                }
            } catch (Exception ex) {
                System.out.println("ℹ️ Note on transaction reset: " + ex.getMessage());
            }

            // 3. Ensure Primary Bank Account starts at ₹0.0
            Account primaryAccount = accountRepository.findByUserAndPrimaryAccountTrue(user).orElse(null);
            if (primaryAccount == null) {
                primaryAccount = Account.builder()
                        .user(user)
                        .accountName("SBI Primary Account")
                        .bankName("State Bank of India")
                        .upiId("keerthana@oksbi")
                        .currentBalance(0.0)
                        .accountType("SAVINGS")
                        .primaryAccount(true)
                        .build();
                accountRepository.save(primaryAccount);
                System.out.println("✅ Primary Bank Account created with balance: ₹0.0");
            } else {
                primaryAccount.setCurrentBalance(0.0);
                accountRepository.save(primaryAccount);
                System.out.println("✅ Primary Bank Account reset to: ₹0.0");
            }

            // 4. Ensure Physical Cash Wallet starts at ₹0.0
            List<Account> allAccounts = accountRepository.findByUser(user);
            boolean cashExists = allAccounts.stream().anyMatch(a ->
                    "CASH".equalsIgnoreCase(a.getAccountType()) ||
                    "Cash Wallet".equalsIgnoreCase(a.getAccountName()));

            if (!cashExists) {
                Account cashWallet = Account.builder()
                        .user(user)
                        .accountName("Cash Wallet")
                        .bankName("Physical Cash")
                        .upiId("cash@wallet")
                        .currentBalance(0.0)
                        .accountType("CASH")
                        .primaryAccount(false)
                        .build();
                accountRepository.save(cashWallet);
                System.out.println("✅ Cash Wallet created with balance: ₹0.0");
            } else {
                allAccounts.stream()
                        .filter(a -> "CASH".equalsIgnoreCase(a.getAccountType()) || "Cash Wallet".equalsIgnoreCase(a.getAccountName()))
                        .forEach(a -> {
                            a.setCurrentBalance(0.0);
                            accountRepository.save(a);
                        });
                System.out.println("✅ Cash Wallet reset to: ₹0.0");
            }

            // 5. Ensure Monthly Budget exists
            List<Budget> budgets = budgetRepository.findByUser(user);
            if (budgets.isEmpty()) {
                Budget budget = Budget.builder()
                        .user(user)
                        .category("Overall Monthly")
                        .limitAmount(25000.0)
                        .period("MONTHLY")
                        .build();
                budgetRepository.save(budget);
                System.out.println("✅ Default monthly budget set to ₹25,000");
            }

            System.out.println("==================================================");
            System.out.println("🎉 [EXPENSE AI] Ready! Listening on http://localhost:8080");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("⚠️ DataInitializer safe note: " + e.getMessage());
        }
    }
}
