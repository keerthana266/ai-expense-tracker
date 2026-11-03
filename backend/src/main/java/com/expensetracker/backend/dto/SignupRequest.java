package com.expensetracker.backend.dto;

import lombok.Data;

@Data
public class SignupRequest {

    private String fullName;

    private String email;

    private String password;

}