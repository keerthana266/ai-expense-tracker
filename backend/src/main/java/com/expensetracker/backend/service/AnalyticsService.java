package com.expensetracker.backend.service;

import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Map<String, Object> getSummary() {

        User user = getLoggedInUser();

        Map<String, Object> summary = new HashMap<>();

        summary.put(
                "totalExpense",
                transactionRepository.getTotalExpense(user)
        );

        summary.put(
                "averageExpense",
                transactionRepository.getAverageExpense(user)
        );

        summary.put(
                "highestExpense",
                transactionRepository.getHighestExpense(user)
        );

        summary.put(
                "lowestExpense",
                transactionRepository.getLowestExpense(user)
        );

        summary.put(
                "merchantSummary",
                transactionRepository.getMerchantSummary(user)
        );

        summary.put(
                "categorySummary",
                transactionRepository.getCategorySummary(user)
        );

        summary.put(
                "monthlyExpenses",
                transactionRepository.getMonthlyExpenses(user)
        );

        return summary;
    }

    public Double getTotalExpense() {

        User user = getLoggedInUser();

        return transactionRepository.getTotalExpense(user);
    }

    public Double getAverageExpense() {

        User user = getLoggedInUser();

        return transactionRepository.getAverageExpense(user);
    }

    public Double getHighestExpense() {

        User user = getLoggedInUser();

        return transactionRepository.getHighestExpense(user);
    }

    public Double getLowestExpense() {

        User user = getLoggedInUser();

        return transactionRepository.getLowestExpense(user);
    }

    public List<Object[]> getMerchantSummary() {

        User user = getLoggedInUser();

        return transactionRepository.getMerchantSummary(user);
    }

    public List<Object[]> getCategorySummary() {

        User user = getLoggedInUser();

        return transactionRepository.getCategorySummary(user);
    }

    public List<Object[]> getMonthlyExpenses() {

        User user = getLoggedInUser();

        return transactionRepository.getMonthlyExpenses(user);
    }
}