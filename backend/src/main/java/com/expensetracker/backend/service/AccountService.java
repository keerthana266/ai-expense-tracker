package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AccountRequest;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public List<Account> getAccounts() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByUser(user);
    }

    public Account addAccount(AccountRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(request.getPrimaryAccount())) {

            List<Account> existingAccounts =
                    accountRepository.findByUser(user);

            for (Account account : existingAccounts) {

                account.setPrimaryAccount(false);

            }

            accountRepository.saveAll(existingAccounts);
        }

        Account account = Account.builder()
                .user(user)
                .bankName(request.getBankName())
                .accountName(request.getAccountName())
                .upiId(request.getUpiId())
                .currentBalance(request.getCurrentBalance())
                .accountType(request.getAccountType())
                .primaryAccount(request.getPrimaryAccount())
                .build();

        return accountRepository.save(account);
    }
}