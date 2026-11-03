package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.BudgetAlertResponse;
import com.expensetracker.backend.dto.PredictionResponse;
import com.expensetracker.backend.dto.ReportResponse;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRoastService roastService;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private BudgetAlertService budgetAlertService;

    public ReportResponse generateReport() {

        // Get logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Total expense for this user
        Double totalExpense =
                transactionRepository.getTotalExpense(user);

        if (totalExpense == null) {
            totalExpense = 0.0;
        }

        // Transaction count for this user
        Long transactionCount =
                transactionRepository.findByUserOrderByTransactionTimeDesc(user)
                        .stream()
                        .count();

        // Top merchant for this user
        String topMerchant = "No Data";

        List<Object[]> merchantSummary =
                transactionRepository.getMerchantSummary(user);

        if (!merchantSummary.isEmpty()
                && merchantSummary.get(0)[0] != null) {

            topMerchant =
                    merchantSummary.get(0)[0].toString();
        }

        // Top category for this user
        String topCategory = "No Data";

        List<Object[]> categorySummary =
                transactionRepository.getCategorySummary(user);

        if (!categorySummary.isEmpty()
                && categorySummary.get(0)[0] != null) {

            topCategory =
                    categorySummary.get(0)[0].toString();
        }

        String roast = roastService.generateRoast();

        PredictionResponse prediction =
                predictionService.predict();

        BudgetAlertResponse budget =
                budgetAlertService.getFoodBudgetAlert();

        return new ReportResponse(
                totalExpense,
                transactionCount,
                topMerchant,
                topCategory,
                roast,
                prediction,
                budget
        );
    }
}