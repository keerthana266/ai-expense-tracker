package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.PredictionResponse;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.Budget;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.BudgetRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PredictionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private static final int RECENT_DAYS = 30;
    private static final int MAX_PREDICTION_DAYS = 365;

    public PredictionResponse predict() {
        // 1. Get logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            email = authentication.getName();
        }

        User user;
        if (email != null && !email.trim().isEmpty()) {
            user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("User not found")));
        } else {
            user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No registered user found in database"));
            email = user.getEmail();
        }

        // 2. Total liquid funds (Bank balance + Cash Wallet)
        Account primaryAccount = accountRepository.findByUserAndPrimaryAccountTrue(user).orElse(null);
        double bankBalance = (primaryAccount != null && primaryAccount.getCurrentBalance() != null)
                ? primaryAccount.getCurrentBalance()
                : 0.0;

        List<Account> allUserAccounts = accountRepository.findByUser(user);
        double cashBalance = allUserAccounts.stream()
                .filter(acc -> {
                    String type = acc.getAccountType() != null ? acc.getAccountType() : "";
                    String name = acc.getAccountName() != null ? acc.getAccountName() : "";
                    return type.equalsIgnoreCase("CASH") || type.equalsIgnoreCase("PHYSICAL CASH")
                            || name.equalsIgnoreCase("Cash Wallet") || name.equalsIgnoreCase("Cash");
                })
                .mapToDouble(acc -> acc.getCurrentBalance() != null ? acc.getCurrentBalance() : 0.0)
                .sum();

        double currentBalance = bankBalance + cashBalance;

        // 3. User Transactions
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionTimeDesc(user);

        List<Transaction> expenses = transactions.stream()
                .filter(t -> t != null && "EXPENSE".equalsIgnoreCase(t.getTransactionType())
                        && t.getAmount() != null && t.getAmount() > 0 && t.getTransactionTime() != null)
                .toList();

        double totalIncome = transactions.stream()
                .filter(t -> t != null && "INCOME".equalsIgnoreCase(t.getTransactionType())
                        && t.getAmount() != null && t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // 4. Safe Daily Speed Limit calculation based on Monthly Budget / Income
        LocalDate today = LocalDate.now();
        List<Budget> budgets = budgetRepository.findByUser(user);
        double configuredBudget = budgets.stream()
                .mapToDouble(b -> b.getLimitAmount() != null ? b.getLimitAmount() : 0.0)
                .sum();

        double monthlySpendingCap;
        if (configuredBudget > 0) {
            monthlySpendingCap = configuredBudget;
        } else if (totalIncome > 0) {
            monthlySpendingCap = totalIncome * 0.70; // 70% rule
        } else if (currentBalance > 0) {
            monthlySpendingCap = Math.min(Math.max(currentBalance * 0.15, 15000.0), 45000.0);
        } else {
            monthlySpendingCap = 15000.0;
        }

        double safeDailyExpense = Math.max(100.0, Math.round((monthlySpendingCap / 30.0) * 100.0) / 100.0);

        // 5. Handle edge case: No recorded expenses yet
        if (expenses.isEmpty()) {
            int defaultDays = currentBalance > 0 ? (int) Math.min(currentBalance / safeDailyExpense, MAX_PREDICTION_DAYS) : 0;
            LocalDate brokeDate = today.plusDays(Math.max(defaultDays, 30));
            return PredictionResponse.builder()
                    .currentBalance(currentBalance)
                    .averageExpense(0.0)
                    .daysLeft(Math.max(defaultDays, 30))
                    .predictedDate(brokeDate.toString())
                    .advice("No expenses recorded yet. Your safe spending pace is ₹" + String.format("%.0f", safeDailyExpense) + "/day.")
                    .safeDailyExpense(safeDailyExpense)
                    .spendingSpeedRatio(0.0)
                    .speedStatus("OPTIMAL")
                    .speedAdvice("Cruising safely. Safe spending speed limit is ₹" + String.format("%.0f", safeDailyExpense) + "/day.")
                    .build();
        }

        // 6. Realistic Burn Rate Calculation
        LocalDateTime recentStart = LocalDateTime.now().minusDays(RECENT_DAYS);
        List<Transaction> recentExpenses = expenses.stream()
                .filter(t -> !t.getTransactionTime().isBefore(recentStart))
                .toList();

        List<Transaction> targetExpenses = !recentExpenses.isEmpty() ? recentExpenses : expenses;
        double totalExpense = targetExpenses.stream().mapToDouble(Transaction::getAmount).sum();

        LocalDate oldestExpenseDate = targetExpenses.stream()
                .map(t -> t.getTransactionTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(today);

        long calendarDays = ChronoUnit.DAYS.between(oldestExpenseDate, today) + 1;
        calendarDays = Math.max(1, Math.min(calendarDays, 30));

        // Count distinct active transaction days to prevent dilute division (e.g. 1 small spend over 90 days)
        long distinctActiveDays = targetExpenses.stream()
                .map(t -> t.getTransactionTime().toLocalDate())
                .distinct()
                .count();
        if (distinctActiveDays < 1) distinctActiveDays = 1;

        // Accurate burn velocity
        double averageDailyExpense = totalExpense / calendarDays;
        if (targetExpenses.size() <= 3 && calendarDays > 7) {
            // Normalize when transaction history is sparse
            averageDailyExpense = Math.max(averageDailyExpense, totalExpense / (distinctActiveDays * 2.5));
        }
        averageDailyExpense = Math.round(averageDailyExpense * 100.0) / 100.0;

        // 7. Calculate Runway Days & Broke Date with realistic caps
        int daysLeft;
        LocalDate displayedPredictedDate;

        if (currentBalance <= 0) {
            daysLeft = 0;
            displayedPredictedDate = today;
        } else if (averageDailyExpense <= 0) {
            daysLeft = MAX_PREDICTION_DAYS;
            displayedPredictedDate = today.plusDays(MAX_PREDICTION_DAYS);
        } else {
            double rawDays = currentBalance / averageDailyExpense;
            if (rawDays > MAX_PREDICTION_DAYS) {
                // If runway is over 1 year (e.g. 2.8 Lakh balance with small spending),
                // cap to 365 days / 1-year horizon so we never display absurd dates like year 2088
                daysLeft = MAX_PREDICTION_DAYS;
                displayedPredictedDate = today.plusDays(MAX_PREDICTION_DAYS);
            } else {
                daysLeft = Math.max(1, (int) Math.floor(rawDays));
                displayedPredictedDate = today.plusDays(daysLeft);
            }
        }

        // 8. Spending Velocity & Speed Pacing Controller
        double speedRatio = safeDailyExpense > 0 ? (averageDailyExpense / safeDailyExpense) : 1.0;
        speedRatio = Math.round(speedRatio * 10.0) / 10.0;

        String speedStatus;
        String speedAdvice;

        if (currentBalance <= 0) {
            speedStatus = "DEPLETED";
            speedAdvice = "🚨 Your balance is zero. Stop all discretionary spending immediately!";
        } else if (speedRatio <= 1.0) {
            speedStatus = "OPTIMAL";
            speedAdvice = String.format("🟢 Cruising Safely: Burning ₹%.0f/day within your safe limit of ₹%.0f/day. Runway is healthy!",
                    averageDailyExpense, safeDailyExpense);
        } else if (speedRatio <= 1.3) {
            speedStatus = "MODERATE";
            speedAdvice = String.format("🟡 Moderate Speed: Burning ₹%.0f/day (Safe limit: ₹%.0f/day). Slow down slightly to preserve runway.",
                    averageDailyExpense, safeDailyExpense);
        } else {
            speedStatus = "SPEEDING_DANGEROUSLY";
            speedAdvice = String.format("🚨 Speeding Alert! Burning money at %.1fx safe speed (₹%.0f/day vs safe limit of ₹%.0f/day). Balance hits ₹0 around %s!",
                    speedRatio, averageDailyExpense, safeDailyExpense, displayedPredictedDate);
        }

        String advice = generateAdvice(currentBalance, averageDailyExpense, daysLeft, targetExpenses.size());

        return PredictionResponse.builder()
                .currentBalance(currentBalance)
                .averageExpense(averageDailyExpense)
                .daysLeft(daysLeft)
                .predictedDate(displayedPredictedDate.toString())
                .advice(advice)
                .safeDailyExpense(safeDailyExpense)
                .spendingSpeedRatio(speedRatio)
                .speedStatus(speedStatus)
                .speedAdvice(speedAdvice)
                .build();
    }

    private String generateAdvice(double currentBalance, double averageDailyExpense, int daysLeft, int expenseCount) {
        if (currentBalance <= 0) {
            return "Your liquid balance is depleted. Pause all non-essential expenses and review subscriptions.";
        }
        if (daysLeft >= MAX_PREDICTION_DAYS) {
            return "Exceptional financial runway! Your balance comfortably covers 12+ months of spending at this pace.";
        }
        if (daysLeft <= 7) {
            return "🚨 CRITICAL: Balance running out in under a week! Stop delivery orders and impulse purchases immediately.";
        }
        if (daysLeft <= 30) {
            return "⚠️ Runway under 30 days. Consider cutting dining out and discretionary shopping to extend runway.";
        }
        if (daysLeft <= 90) {
            return "Manageable 3-month runway. Stay mindful of larger one-off purchases.";
        }
        return "Spending pace is stable. Keep tracking transactions to maintain financial health.";
    }
}
