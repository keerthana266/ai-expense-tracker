package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.BudgetAlertResponse;
import com.expensetracker.backend.entity.Budget;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.BudgetRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetAlertService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // GET LOGGED-IN USER
    // =========================================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName()
                        .equals("anonymousUser")) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + email
                        )
                );
    }


    // =========================================================
    // CREATE / UPDATE BUDGET
    // =========================================================

    public Budget saveBudget(
            String category,
            Double limitAmount,
            String period) {

        User user = getLoggedInUser();

        if (category == null || category.trim().isEmpty()) {
            throw new RuntimeException("Category is required");
        }

        if (limitAmount == null || limitAmount <= 0) {
            throw new RuntimeException(
                    "Budget amount must be greater than zero"
            );
        }

        if (period == null || period.trim().isEmpty()) {
            period = "MONTHLY";
        }

        Budget budget =
                budgetRepository
                        .findByUserAndCategory(
                                user,
                                category.trim()
                        )
                        .orElse(null);

        if (budget == null) {

            budget = Budget.builder()
                    .user(user)
                    .category(category.trim())
                    .limitAmount(limitAmount)
                    .period(period.toUpperCase())
                    .build();

        } else {

            budget.setLimitAmount(limitAmount);
            budget.setPeriod(period.toUpperCase());
        }

        return budgetRepository.save(budget);
    }


    // =========================================================
    // GET USER BUDGETS
    // =========================================================

    public List<Budget> getBudgets() {

        User user = getLoggedInUser();

        return budgetRepository.findByUser(user);
    }


    // =========================================================
    // FOOD BUDGET ALERT
    // =========================================================

    public BudgetAlertResponse getFoodBudgetAlert() {

        User user = getLoggedInUser();

        // Food category used by the application
        String category = "Food";

        Budget budget =
                budgetRepository
                        .findByUserAndCategory(
                                user,
                                category
                        )
                        .orElse(null);

        // If no Food budget exists, return a safe default response
        if (budget == null) {

            return BudgetAlertResponse.builder()
                    .budgetAmount(0.0)
                    .spentAmount(0.0)
                    .remainingAmount(0.0)
                    .percentageUsed(0.0)
                    .status("NO_BUDGET")
                    .build();
        }


        // -----------------------------------------------------
        // Current month's date range
        // -----------------------------------------------------

        LocalDate today = LocalDate.now();

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate = today;


        // -----------------------------------------------------
        // Get user's transactions
        // -----------------------------------------------------

        List<Transaction> transactions =
                transactionRepository
                        .findByUserOrderByTransactionTimeDesc(
                                user
                        );


        // -----------------------------------------------------
        // Calculate food spending
        // -----------------------------------------------------

        double spentAmount =
                transactions.stream()

                        .filter(t ->
                                t.getTransactionType() != null
                                        &&
                                t.getTransactionType()
                                        .equalsIgnoreCase("EXPENSE")
                        )

                        .filter(t ->
                                t.getCategory() != null
                                        &&
                                t.getCategory().getName() != null
                        )

                        .filter(t ->
                                t.getCategory()
                                        .getName()
                                        .equalsIgnoreCase(category)
                        )

                        .filter(t ->
                                t.getTransactionTime() != null
                        )

                        .filter(t -> {

                            LocalDate date =
                                    t.getTransactionTime()
                                            .toLocalDate();

                            return !date.isBefore(startDate)
                                    && !date.isAfter(endDate);
                        })

                        .mapToDouble(t ->
                                t.getAmount() == null
                                        ? 0.0
                                        : t.getAmount()
                        )

                        .sum();


        // -----------------------------------------------------
        // Calculate values
        // -----------------------------------------------------

        double limit =
                budget.getLimitAmount();

        double remaining =
                limit - spentAmount;

        double percentageUsed = 0.0;

        if (limit > 0) {

            percentageUsed =
                    (spentAmount / limit) * 100.0;
        }

        percentageUsed =
                Math.round(
                        percentageUsed * 100.0
                ) / 100.0;


        // -----------------------------------------------------
        // Determine status
        // -----------------------------------------------------

        String status;

        if (percentageUsed >= 100) {

            status = "EXCEEDED";

        } else if (percentageUsed >= 80) {

            status = "WARNING";

        } else {

            status = "SAFE";
        }


        // -----------------------------------------------------
        // Return DTO expected by Controller and ReportService
        // -----------------------------------------------------

        return BudgetAlertResponse.builder()
                .budgetAmount(
                        Math.round(limit * 100.0) / 100.0
                )
                .spentAmount(
                        Math.round(spentAmount * 100.0) / 100.0
                )
                .remainingAmount(
                        Math.round(remaining * 100.0) / 100.0
                )
                .percentageUsed(percentageUsed)
                .status(status)
                .build();
    }


    // =========================================================
    // GET BUDGET STATUS
    // =========================================================

    public BudgetStatus getBudgetStatus(String category) {

        User user = getLoggedInUser();

        Budget budget =
                budgetRepository
                        .findByUserAndCategory(
                                user,
                                category
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Budget not found for category: "
                                                + category
                                )
                        );


        LocalDate today = LocalDate.now();

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate = today;


        List<Transaction> transactions =
                transactionRepository
                        .findByUserOrderByTransactionTimeDesc(
                                user
                        );


        double spentAmount =
                transactions.stream()

                        .filter(t ->
                                t.getTransactionType() != null
                                        &&
                                t.getTransactionType()
                                        .equalsIgnoreCase("EXPENSE")
                        )

                        .filter(t ->
                                t.getCategory() != null
                                        &&
                                t.getCategory().getName() != null
                        )

                        .filter(t ->
                                t.getCategory()
                                        .getName()
                                        .equalsIgnoreCase(category)
                        )

                        .filter(t ->
                                t.getTransactionTime() != null
                        )

                        .filter(t -> {

                            LocalDate date =
                                    t.getTransactionTime()
                                            .toLocalDate();

                            return !date.isBefore(startDate)
                                    && !date.isAfter(endDate);
                        })

                        .mapToDouble(t ->
                                t.getAmount() == null
                                        ? 0.0
                                        : t.getAmount()
                        )

                        .sum();


        double limit =
                budget.getLimitAmount();

        double remaining =
                limit - spentAmount;

        double percentageUsed = 0.0;

        if (limit > 0) {

            percentageUsed =
                    (spentAmount / limit) * 100.0;
        }

        percentageUsed =
                Math.round(
                        percentageUsed * 100.0
                ) / 100.0;


        String status;

        if (percentageUsed >= 100) {

            status = "EXCEEDED";

        } else if (percentageUsed >= 80) {

            status = "WARNING";

        } else {

            status = "SAFE";
        }


        return BudgetStatus.builder()
                .category(budget.getCategory())
                .budgetAmount(
                        Math.round(limit * 100.0) / 100.0
                )
                .spentAmount(
                        Math.round(spentAmount * 100.0) / 100.0
                )
                .remainingAmount(
                        Math.round(remaining * 100.0) / 100.0
                )
                .percentageUsed(percentageUsed)
                .status(status)
                .build();
    }


    // =========================================================
    // DELETE BUDGET
    // =========================================================

    public void deleteBudget(Long id) {

        User user = getLoggedInUser();

        Budget budget =
                budgetRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Budget not found"
                                )
                        );

        if (!budget.getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You cannot delete another user's budget"
            );
        }

        budgetRepository.delete(budget);
    }


    // =========================================================
    // BUDGET STATUS DTO
    // =========================================================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BudgetStatus {

        private String category;

        private double budgetAmount;

        private double spentAmount;

        private double remainingAmount;

        private double percentageUsed;

        private String status;
    }
}