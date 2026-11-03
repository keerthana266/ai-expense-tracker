package com.expensetracker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertResponse {

    private double budgetAmount;

    private double spentAmount;

    private double remainingAmount;

    private double percentageUsed;

    private String status;
}