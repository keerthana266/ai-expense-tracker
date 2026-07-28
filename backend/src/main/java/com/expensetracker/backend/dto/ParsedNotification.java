package com.expensetracker.backend.dto;

import lombok.Data;

@Data
public class ParsedNotification {

    private String merchant;
    private Double amount;
    private String paymentMethod;
    private String category;
}