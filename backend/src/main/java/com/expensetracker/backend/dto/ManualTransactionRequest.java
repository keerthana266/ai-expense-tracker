package com.expensetracker.backend.dto;

import lombok.Data;

@Data
public class ManualTransactionRequest {

    private Long accountId;

    private Double amount;

    private String merchant;

    private String category;

    private String paymentMethod;

    private String transactionType;

    private String notificationText;
}