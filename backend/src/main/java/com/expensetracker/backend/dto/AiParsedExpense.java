package com.expensetracker.backend.dto;

import lombok.Data;

@Data
public class AiParsedExpense {

    private String merchant;

    private double amount;

    private String paymentMethod;

    private String category;

    private double confidence;

    private String reason;
}