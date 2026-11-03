package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.DashboardResponse;
import com.expensetracker.backend.service.DashboardService;
import com.expensetracker.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private GeminiService geminiService;


    // =========================================================
    // MAIN DASHBOARD
    // =========================================================

    @GetMapping
    public DashboardResponse getDashboard() {

        return dashboardService.getDashboardData();
    }


    // =========================================================
    // MERCHANT SUMMARY
    // =========================================================

    @GetMapping("/merchant-summary")
    public List<Object[]> getMerchantSummary() {

        return dashboardService.getMerchantSummary();
    }


    // =========================================================
    // CATEGORY SUMMARY
    // =========================================================

    @GetMapping("/category-summary")
    public List<Object[]> getCategorySummary() {

        return dashboardService.getCategorySummary();
    }


    // =========================================================
    // ACCOUNT-WISE ANALYTICS
    // =========================================================

    @GetMapping("/account-summary")
    public List<Map<String, Object>> getAccountWiseAnalytics() {

        return dashboardService.getAccountWiseAnalytics();
    }


    // =========================================================
    // ACCOUNT + CATEGORY ANALYTICS
    // =========================================================

    @GetMapping("/account-category-summary")
    public List<Map<String, Object>> getAccountCategorySummary() {

        return dashboardService.getAccountCategorySummary();
    }


    // =========================================================
    // ACCOUNT-WISE MONTHLY EXPENSES
    // =========================================================

    @GetMapping("/account-monthly-expenses")
    public List<Map<String, Object>> getAccountMonthlyExpenseSummary() {

        return dashboardService.getAccountMonthlyExpenseSummary();
    }


    // =========================================================
    // AI FINANCIAL INSIGHTS
    // =========================================================

    @PostMapping("/ai-insights")
    public String getAiInsights(
            @RequestBody Map<String, Object> analyticsData
    ) {

        String prompt = """
                You are SpendWise AI, a personal finance assistant.

                Analyse the user's spending analytics below.

                Give exactly 3 concise personalized financial insights.

                Rules:
                - Use ONLY the supplied data.
                - Do not invent transactions.
                - Do not invent amounts.
                - Mention actual categories, amounts or percentages when useful.
                - Give practical advice.
                - Keep each insight short.
                - Return ONLY valid JSON.

                Required format:

                {
                  "insights": [
                    {
                      "icon": "🍔",
                      "title": "Short title",
                      "message": "Short personalized explanation"
                    },
                    {
                      "icon": "📈",
                      "title": "Short title",
                      "message": "Short personalized explanation"
                    },
                    {
                      "icon": "💰",
                      "title": "Short title",
                      "message": "Short personalized explanation"
                    }
                  ]
                }

                User analytics:
                """ + analyticsData;

        return geminiService.askGemini(prompt);
    }


    // =========================================================
    // AI ROAST MODE
    // =========================================================

    @PostMapping("/ai-roast")
    public String getAiRoast(
            @RequestBody Map<String, Object> analyticsData
    ) {

        System.out.println("========================================");
        System.out.println("🔥 AI ROAST REQUEST");
        System.out.println("========================================");

        System.out.println("Analytics received:");
        System.out.println(analyticsData);

        try {

            String prompt = """
                    You are SpendWise Roast Mode, a funny but helpful personal finance assistant.

                    Analyse the user's spending analytics below.

                    Generate exactly ONE short personalized financial roast.

                    Rules:
                    - Use ONLY the supplied data.
                    - Do not invent transactions.
                    - Do not invent amounts.
                    - Mention actual amounts or categories when useful.
                    - Be funny but helpful.
                    - Do not be offensive.
                    - Maximum 2 sentences.
                    - Include one suitable emoji.
                    - Do NOT return JSON.
                    - Do NOT return an object.
                    - Do NOT return an "insights" array.
                    - Return ONLY plain text.

                    Example:

                    🍔 You spent ₹1,500 on food this month — your wallet clearly thinks you're running a restaurant!

                    User analytics:
                    """ + analyticsData;

            String roast = geminiService.askGeminiPlainText(prompt);

            /*
             * If Gemini returned a usable response,
             * return it directly.
             */
            if (roast != null && !roast.isBlank()) {

                return roast.trim();
            }

            /*
             * If Gemini returned nothing,
             * generate local fallback.
             */
            return generateFallbackRoast(analyticsData);

        } catch (Exception e) {

            System.out.println("========================================");
            System.out.println("⚠️ AI ROAST FAILED");
            System.out.println(e.getMessage());
            System.out.println("Using fallback roast.");
            System.out.println("========================================");

            return generateFallbackRoast(analyticsData);
        }
    }


    // =========================================================
    // PERSONALIZED FALLBACK ROAST
    // =========================================================

    private String generateFallbackRoast(
            Map<String, Object> analyticsData
    ) {

        double income =
                getDouble(analyticsData.get("totalIncome"));

        double expense =
                getDouble(analyticsData.get("totalExpense"));

        double savings =
                getDouble(analyticsData.get("savings"));

        int transactionCount =
                (int) getDouble(
                        analyticsData.get("transactionCount")
                );


        // -----------------------------------------------------
        // CASE 1: Expenses greater than income
        // -----------------------------------------------------

        if (income > 0 && expense > income) {

            return String.format(
                    "🔥 You earned ₹%.0f but spent ₹%.0f across %d transactions — your wallet is treating income like a temporary visitor! 💸",
                    income,
                    expense,
                    transactionCount
            );
        }


        // -----------------------------------------------------
        // CASE 2: User has positive savings
        // -----------------------------------------------------

        if (savings > 0) {

            return String.format(
                    "💰 You actually saved ₹%.0f — look at you giving your wallet a reason to stay alive! 🔥",
                    savings
            );
        }


        // -----------------------------------------------------
        // CASE 3: Expenses exist but no income data
        // -----------------------------------------------------

        if (expense > 0) {

            return String.format(
                    "🔥 ₹%.0f has already left your wallet across %d transactions — maybe give that money a chance to meet you again! 💸",
                    expense,
                    transactionCount
            );
        }


        // -----------------------------------------------------
        // CASE 4: No spending
        // -----------------------------------------------------

        return "🤖 Your wallet is suspiciously quiet today — even SpendWise has nothing to roast!";
    }


    // =========================================================
    // SAFE NUMBER CONVERSION
    // =========================================================

    private double getDouble(Object value) {

        if (value == null) {

            return 0.0;
        }

        if (value instanceof Number) {

            return ((Number) value).doubleValue();
        }

        try {

            return Double.parseDouble(
                    value.toString()
            );

        } catch (Exception e) {

            return 0.0;
        }
    }
}