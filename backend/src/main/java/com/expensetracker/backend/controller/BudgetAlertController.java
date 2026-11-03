package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.BudgetAlertResponse;
import com.expensetracker.backend.entity.Budget;
import com.expensetracker.backend.service.BudgetAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin("*")
public class BudgetAlertController {

    @Autowired
    private BudgetAlertService budgetAlertService;

    @GetMapping
    public BudgetAlertResponse getBudgetAlert() {
        return budgetAlertService.getFoodBudgetAlert();
    }

    @PostMapping
    public Budget setBudget(@RequestBody Map<String, Object> body) {
        String category = (String) body.getOrDefault("category", "Overall Monthly");
        Double amount = 25000.0;
        try {
            amount = Double.parseDouble(String.valueOf(body.get("amount")));
        } catch (Exception ignored) {}
        String period = (String) body.getOrDefault("period", "MONTHLY");

        return budgetAlertService.saveBudget(category, amount, period);
    }
}
