package com.expensetracker.backend.controller;

import com.expensetracker.backend.service.RoastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roast")
@CrossOrigin(origins = "*")
public class RoastController {

    @Autowired
    private RoastService roastService;

    @GetMapping
    public String getRoast(
            @RequestParam double totalExpense,
            @RequestParam String topMerchant,
            @RequestParam String topCategory
    ) {

        System.out.println("========== ROAST REQUEST ==========");

        System.out.println("Total Expense : ₹" + totalExpense);
        System.out.println("Top Merchant  : " + topMerchant);
        System.out.println("Top Category  : " + topCategory);

        String roast = roastService.generateRoast(
                totalExpense,
                topMerchant,
                topCategory
        );

        System.out.println("AI Roast      : " + roast);
        System.out.println("===================================");

        return roast;
    }
}