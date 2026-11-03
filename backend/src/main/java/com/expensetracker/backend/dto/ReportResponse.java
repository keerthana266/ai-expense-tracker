package com.expensetracker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportResponse {

    private Double totalExpense;

    private Long transactionCount;

    private String topMerchant;

    private String topCategory;

    private String roast;

    private PredictionResponse prediction;

    private BudgetAlertResponse budget;
}