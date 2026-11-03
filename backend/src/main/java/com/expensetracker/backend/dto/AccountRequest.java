package com.expensetracker.backend.dto;

import lombok.Data;

@Data
public class AccountRequest {

    private String bankName;

    private String accountName;

    private String upiId;

    private Double currentBalance;

    private String accountType;

    private Boolean primaryAccount;

}