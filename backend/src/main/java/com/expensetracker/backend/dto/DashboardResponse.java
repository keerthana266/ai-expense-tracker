package com.expensetracker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // ============================================
    // BASIC DASHBOARD DATA
    // ============================================

    private double totalBalance;

    private double totalIncome;

    private double totalExpense;

    private double savings;

    private long transactionCount;


    // ============================================
    // EXISTING FEATURES
    // ============================================

    private String roast;

    private Object prediction;

    private Object budget;


    // ============================================
    // MONTHLY EXPENSES
    // ============================================

    private List<Object[]> monthlyExpenses;


    // ============================================
    // NEW: SPENDING INSIGHTS
    // ============================================

    private SpendingInsights spendingInsights;


    // ============================================
    // INNER DTO
    // ============================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendingInsights {

        // Highest individual expense
        private double highestExpense;

        // Lowest individual expense
        private double lowestExpense;

        // Average expense
        private double averageExpense;

        // Merchant where user spends the most
        private String topMerchant;

        // Category where user spends the most
        private String topCategory;

        // Total number of expenses
        private long expenseCount;

        // Simple percentage of total transactions that are expenses
        private double expensePercentage;
    }
}