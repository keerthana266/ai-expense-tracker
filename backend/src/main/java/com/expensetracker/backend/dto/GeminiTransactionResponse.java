package com.expensetracker.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiTransactionResponse {

    private String merchant;
    private double amount;
    private String paymentMethod;
    private String category;
    private double confidence;
    private String reasoning;
}