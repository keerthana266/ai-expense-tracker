package com.expensetracker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {

    private Double currentBalance;
    private Double averageExpense;
    private Integer daysLeft;
    private String predictedDate;
    private String advice;

    // Spending Speed & Pacing Controller (Tells user to "Speed Slowly")
    private Double safeDailyExpense;
    private Double spendingSpeedRatio;
    private String speedStatus;     // "OPTIMAL", "MODERATE", "SPEEDING_DANGEROUSLY"
    private String speedAdvice;

    // Backward-compatible constructor for existing callers
    public PredictionResponse(
            Double currentBalance,
            Double averageExpense,
            Integer daysLeft,
            String predictedDate,
            String advice) {
        this.currentBalance = currentBalance;
        this.averageExpense = averageExpense;
        this.daysLeft = daysLeft;
        this.predictedDate = predictedDate;
        this.advice = advice;
        this.safeDailyExpense = 0.0;
        this.spendingSpeedRatio = 1.0;
        this.speedStatus = "OPTIMAL";
        this.speedAdvice = "Pace your spending sustainably.";
    }
}
