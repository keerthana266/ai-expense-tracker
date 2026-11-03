package com.expensetracker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MerchantSummaryDto {

    private String merchant;
    private Double totalSpent;
}