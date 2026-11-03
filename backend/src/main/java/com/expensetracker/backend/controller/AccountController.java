package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.AccountRequest;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.service.AccountService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin("*")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAccounts() {
        return accountService.getAccounts();
    }

    @PostMapping
    public Account addAccount(
            @RequestBody AccountRequest request
    ) {
        return accountService.addAccount(request);
    }

}