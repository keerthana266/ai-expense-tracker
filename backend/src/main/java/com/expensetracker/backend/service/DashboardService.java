package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.DashboardResponse;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // GET LOGGED-IN USER
    // =========================================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + email
                        )
                );
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    public DashboardResponse getDashboardData() {

        System.out.println();
        System.out.println("========== DASHBOARD START ==========");

        // =====================================================
        // USER
        // =====================================================

        User user = getLoggedInUser();

        System.out.println(
                "Dashboard user: " + user.getEmail()
        );


        // =====================================================
        // TRANSACTIONS
        // =====================================================

        System.out.println(
                "Loading transactions..."
        );

        List<Transaction> transactions =
                transactionRepository
                        .findByUserOrderByTransactionTimeDesc(user);

        System.out.println(
                "Transactions loaded: "
                        + transactions.size()
        );


        // =====================================================
        // TOTAL EXPENSE
        // =====================================================

        double totalExpense =
                transactions.stream()
                        .filter(t ->
                                t.getTransactionType() != null
                                        &&
                                t.getTransactionType()
                                        .equalsIgnoreCase("EXPENSE")
                        )
                        .mapToDouble(t ->
                                t.getAmount() == null
                                        ? 0.0
                                        : t.getAmount()
                        )
                        .sum();

        System.out.println(
                "Total expense: " + totalExpense
        );


        // =====================================================
        // TOTAL INCOME
        // =====================================================

        double totalIncome =
                transactions.stream()
                        .filter(t ->
                                t.getTransactionType() != null
                                        &&
                                t.getTransactionType()
                                        .equalsIgnoreCase("INCOME")
                        )
                        .mapToDouble(t ->
                                t.getAmount() == null
                                        ? 0.0
                                        : t.getAmount()
                        )
                        .sum();

        System.out.println(
                "Total income: " + totalIncome
        );


        // =====================================================
        // COMBINED ACCOUNT BALANCE
        // =====================================================

        System.out.println(
                "Loading all user accounts..."
        );

        List<Account> accounts =
                accountRepository.findByUser(user);

        double totalBalance = 0.0;

        if (accounts != null) {

            for (Account currentAccount : accounts) {

                if (currentAccount == null) {
                    continue;
                }

                Double balance =
                        currentAccount.getCurrentBalance();

                if (balance == null) {
                    balance = 0.0;
                }

                totalBalance += balance;

                System.out.println(
                        "Account: "
                                + currentAccount.getAccountName()
                                + " | Type: "
                                + currentAccount.getAccountType()
                                + " | Balance: "
                                + balance
                );
            }
        }

        totalBalance =
                Math.round(
                        totalBalance * 100.0
                ) / 100.0;

        System.out.println(
                "Combined total balance: "
                        + totalBalance
        );


        // =====================================================
        // SAVINGS
        // =====================================================

        double savings =
                totalIncome - totalExpense;

        System.out.println(
                "Savings: " + savings
        );


        // =====================================================
        // TOP MERCHANT
        // =====================================================

        System.out.println(
                "Loading merchant summary..."
        );

        String topMerchant = "None";

        List<Object[]> merchantSummary =
                transactionRepository
                        .getMerchantSummary(user);

        if (merchantSummary != null
                && !merchantSummary.isEmpty()
                && merchantSummary.get(0) != null
                && merchantSummary.get(0).length > 0
                && merchantSummary.get(0)[0] != null) {

            topMerchant =
                    merchantSummary
                            .get(0)[0]
                            .toString();
        }

        System.out.println(
                "Top merchant: " + topMerchant
        );


        // =====================================================
        // TOP CATEGORY
        // =====================================================

        System.out.println(
                "Loading category summary..."
        );

        String topCategory = "Others";

        List<Object[]> categorySummary =
                transactionRepository
                        .getCategorySummary(user);

        if (categorySummary != null
                && !categorySummary.isEmpty()
                && categorySummary.get(0) != null
                && categorySummary.get(0).length > 0
                && categorySummary.get(0)[0] != null) {

            topCategory =
                    categorySummary
                            .get(0)[0]
                            .toString();
        }

        System.out.println(
                "Top category: " + topCategory
        );


        // =====================================================
        // MONTHLY EXPENSES
        // =====================================================

        System.out.println(
                "Loading monthly expenses..."
        );

        List<Object[]> monthlyExpenses =
                transactionRepository
                        .getMonthlyExpenses(user);

        System.out.println(
                "Monthly expenses loaded: "
                        + (
                            monthlyExpenses == null
                                    ? 0
                                    : monthlyExpenses.size()
                        )
        );


        // =====================================================
        // SPENDING INSIGHTS
        // =====================================================

        System.out.println(
                "Generating spending insights..."
        );

        DashboardResponse.SpendingInsights spendingInsights =
                generateSpendingInsights(
                        user,
                        transactions,
                        totalExpense,
                        topMerchant,
                        topCategory
                );

        System.out.println(
                "Spending insights generated."
        );


        // =====================================================
        // BUDGET
        // =====================================================

        Object budget = null;


        // =====================================================
        // AI ROAST FALLBACK
        // =====================================================

        System.out.println(
                "Generating AI roast..."
        );

        String roast =
                generateFallbackRoast(
                        totalExpense,
                        topMerchant,
                        topCategory
                );

        System.out.println(
                "Roast generated: " + roast
        );


        // =====================================================
        // BALANCE / BROKE DATE PREDICTION
        // =====================================================

        System.out.println(
                "Generating balance prediction..."
        );

        Object prediction =
                generatePrediction(
                        totalBalance,
                        totalExpense,
                        transactions
                );

        System.out.println(
                "Prediction generated: " + prediction
        );


        // =====================================================
        // BUILD RESPONSE
        // =====================================================

        System.out.println(
                "Building dashboard response..."
        );

        DashboardResponse response =
                DashboardResponse.builder()

                        .totalBalance(
                                totalBalance
                        )

                        .totalIncome(
                                totalIncome
                        )

                        .totalExpense(
                                totalExpense
                        )

                        .savings(
                                savings
                        )

                        .transactionCount(
                                transactions.size()
                        )

                        .roast(
                                roast
                        )

                        .prediction(
                                prediction
                        )

                        .budget(
                                budget
                        )

                        .monthlyExpenses(
                                monthlyExpenses
                        )

                        .spendingInsights(
                                spendingInsights
                        )

                        .build();


        System.out.println(
                "========== DASHBOARD END =========="
        );

        System.out.println();

        return response;
    }


    // =========================================================
    // SPENDING INSIGHTS
    // =========================================================

    private DashboardResponse.SpendingInsights generateSpendingInsights(
            User user,
            List<Transaction> transactions,
            double totalExpense,
            String topMerchant,
            String topCategory) {

        List<Transaction> expenses =
                transactions.stream()
                        .filter(t ->
                                t.getTransactionType() != null
                                        &&
                                t.getTransactionType()
                                        .equalsIgnoreCase("EXPENSE")
                                        &&
                                t.getAmount() != null
                                        &&
                                t.getAmount() > 0
                        )
                        .toList();

        long expenseCount =
                expenses.size();

        double highestExpense = 0.0;
        double lowestExpense = 0.0;
        double averageExpense = 0.0;

        if (expenseCount > 0) {

            Double highest =
                    transactionRepository
                            .getHighestExpense(user);

            Double lowest =
                    transactionRepository
                            .getLowestExpense(user);

            Double average =
                    transactionRepository
                            .getAverageExpense(user);

            if (highest != null) {
                highestExpense = highest;
            }

            if (lowest != null) {
                lowestExpense = lowest;
            }

            if (average != null) {
                averageExpense = average;
            }
        }


        // =====================================================
        // EXPENSE PERCENTAGE
        // =====================================================

        double expensePercentage = 0.0;

        if (!transactions.isEmpty()) {

            expensePercentage =
                    ((double) expenseCount
                            / transactions.size())
                            * 100.0;
        }


        // =====================================================
        // ROUND VALUES
        // =====================================================

        highestExpense =
                Math.round(
                        highestExpense * 100.0
                ) / 100.0;

        lowestExpense =
                Math.round(
                        lowestExpense * 100.0
                ) / 100.0;

        averageExpense =
                Math.round(
                        averageExpense * 100.0
                ) / 100.0;

        expensePercentage =
                Math.round(
                        expensePercentage * 100.0
                ) / 100.0;


        // =====================================================
        // BUILD INSIGHTS
        // =====================================================

        return DashboardResponse.SpendingInsights
                .builder()

                .highestExpense(
                        highestExpense
                )

                .lowestExpense(
                        lowestExpense
                )

                .averageExpense(
                        averageExpense
                )

                .topMerchant(
                        topMerchant
                )

                .topCategory(
                        topCategory
                )

                .expenseCount(
                        expenseCount
                )

                .expensePercentage(
                        expensePercentage
                )

                .build();
    }


    // =========================================================
    // BROKE DATE / BALANCE PREDICTION
    // =========================================================

    private Object generatePrediction(
            double currentBalance,
            double totalExpense,
            List<Transaction> transactions) {

        // =====================================================
        // ALL EXPENSES
        // =====================================================

        List<Transaction> expenses =
                transactions.stream()
                        .filter(t ->
                                t.getTransactionType() != null
                                        &&
                                t.getTransactionType()
                                        .equalsIgnoreCase("EXPENSE")
                                        &&
                                t.getAmount() != null
                                        &&
                                t.getAmount() > 0
                        )
                        .toList();


        // =====================================================
        // NO BALANCE
        // =====================================================

        if (currentBalance <= 0) {

            Map<String, Object> prediction =
                    new LinkedHashMap<>();

            prediction.put(
                    "currentBalance",
                    currentBalance
            );

            prediction.put(
                    "averageExpense",
                    0.0
            );

            prediction.put(
                    "averageDailyExpense",
                    0.0
            );

            prediction.put(
                    "daysLeft",
                    0
            );

            prediction.put(
                    "predictedDate",
                    LocalDate.now().toString()
            );

            prediction.put(
                    "brokeDate",
                    LocalDate.now().toString()
            );

            prediction.put(
                    "advice",
                    "Your balance has already reached ₹0. Consider reducing unnecessary spending."
            );

            return prediction;
        }


        // =====================================================
        // NO EXPENSE DATA
        // =====================================================

        if (expenses.isEmpty()) {

            Map<String, Object> prediction =
                    new LinkedHashMap<>();

            prediction.put(
                    "currentBalance",
                    currentBalance
            );

            prediction.put(
                    "averageExpense",
                    0.0
            );

            prediction.put(
                    "averageDailyExpense",
                    0.0
            );

            prediction.put(
                    "daysLeft",
                    0
            );

            prediction.put(
                    "predictedDate",
                    null
            );

            prediction.put(
                    "brokeDate",
                    null
            );

            prediction.put(
                    "advice",
                    "Not enough spending history to predict when your balance may reach ₹0."
            );

            return prediction;
        }


        // =====================================================
        // RECENT 30-DAY SPENDING WINDOW
        // =====================================================

        LocalDate today =
                LocalDate.now();

        LocalDate thirtyDaysAgo =
                today.minusDays(29);

        List<Transaction> recentExpenses =
                expenses.stream()
                        .filter(t ->
                                t.getTransactionTime() != null
                                        &&
                                !t.getTransactionTime()
                                        .toLocalDate()
                                        .isBefore(thirtyDaysAgo)
                                        &&
                                !t.getTransactionTime()
                                        .toLocalDate()
                                        .isAfter(today)
                        )
                        .toList();


        // =====================================================
        // CALCULATE SPENDING RATE
        // =====================================================

        double spendingForPrediction = 0.0;

        long spendingDays = 30;


        if (!recentExpenses.isEmpty()) {

            spendingForPrediction =
                    recentExpenses.stream()
                            .mapToDouble(t ->
                                    t.getAmount() == null
                                            ? 0.0
                                            : t.getAmount()
                            )
                            .sum();

            spendingDays = 30;

        } else {

            // =================================================
            // FALLBACK TO AVAILABLE HISTORY
            // =================================================

            LocalDate earliestDate = null;
            LocalDate latestDate = null;

            for (Transaction transaction : expenses) {

                if (transaction.getTransactionTime() == null) {
                    continue;
                }

                LocalDate date =
                        transaction
                                .getTransactionTime()
                                .toLocalDate();

                if (earliestDate == null
                        || date.isBefore(earliestDate)) {

                    earliestDate = date;
                }

                if (latestDate == null
                        || date.isAfter(latestDate)) {

                    latestDate = date;
                }
            }


            if (earliestDate == null
                    || latestDate == null) {

                spendingDays = 1;

            } else {

                spendingDays =
                        ChronoUnit.DAYS.between(
                                earliestDate,
                                latestDate
                        ) + 1;
            }

            if (spendingDays <= 0) {
                spendingDays = 1;
            }

            spendingForPrediction =
                    totalExpense;
        }


        // =====================================================
        // AVERAGE DAILY EXPENSE
        // =====================================================

        double averageDailyExpense =
                spendingForPrediction
                        / (double) spendingDays;


        System.out.println(
                "Prediction spending window: "
                        + spendingDays
                        + " days"
        );

        System.out.println(
                "Prediction expense amount: ₹"
                        + spendingForPrediction
        );

        System.out.println(
                "Prediction average daily expense: ₹"
                        + averageDailyExpense
        );


        // =====================================================
        // PREVENT DIVISION BY ZERO
        // =====================================================

        if (averageDailyExpense <= 0) {

            Map<String, Object> prediction =
                    new LinkedHashMap<>();

            prediction.put(
                    "currentBalance",
                    currentBalance
            );

            prediction.put(
                    "averageExpense",
                    0.0
            );

            prediction.put(
                    "averageDailyExpense",
                    0.0
            );

            prediction.put(
                    "daysLeft",
                    0
            );

            prediction.put(
                    "predictedDate",
                    null
            );

            prediction.put(
                    "brokeDate",
                    null
            );

            prediction.put(
                    "advice",
                    "Your spending history is not sufficient to estimate a balance runway."
            );

            return prediction;
        }


        // =====================================================
        // DAYS UNTIL ZERO
        // =====================================================

        long daysLeft =
                (long) Math.ceil(
                        currentBalance
                                / averageDailyExpense
                );

        if (daysLeft < 1) {
            daysLeft = 1;
        }


        // =====================================================
        // PREDICTED DATE
        // =====================================================

        LocalDate predictedDate =
                today.plusDays(daysLeft);


        // =====================================================
        // ADVICE
        // =====================================================

        String advice;

        if (daysLeft <= 7) {

            advice =
                    "Your balance could reach ₹0 within a week. Reduce non-essential spending immediately.";

        } else if (daysLeft <= 30) {

            advice =
                    "Your balance runway is less than a month. Consider reducing unnecessary purchases.";

        } else if (daysLeft <= 90) {

            advice =
                    "Your current spending rate is manageable, but reducing discretionary expenses will extend your balance runway.";

        } else {

            advice =
                    "Your current spending rate gives you a comfortable balance runway. Keep monitoring your expenses.";
        }


        // =====================================================
        // BUILD PREDICTION
        // =====================================================

        Map<String, Object> prediction =
                new LinkedHashMap<>();

        prediction.put(
                "currentBalance",
                Math.round(
                        currentBalance * 100.0
                ) / 100.0
        );

        prediction.put(
                "averageExpense",
                Math.round(
                        averageDailyExpense * 100.0
                ) / 100.0
        );

        prediction.put(
                "averageDailyExpense",
                Math.round(
                        averageDailyExpense * 100.0
                ) / 100.0
        );

        prediction.put(
                "daysLeft",
                daysLeft
        );

        prediction.put(
                "predictedDate",
                predictedDate.toString()
        );

        prediction.put(
                "brokeDate",
                predictedDate.toString()
        );

        prediction.put(
                "advice",
                advice
        );

        return prediction;
    }


    // =========================================================
    // FALLBACK ROAST
    // =========================================================

    private String generateFallbackRoast(
            double totalExpense,
            String topMerchant,
            String topCategory) {

        if (totalExpense <= 0) {

            return "Your wallet is surprisingly peaceful today. 😌";
        }

        if (topMerchant != null
                && !topMerchant.equalsIgnoreCase("None")) {

            return String.format(
                    "%s seems to know your wallet better than you do. 💸",
                    topMerchant
            );
        }

        if (topCategory != null
                && !topCategory.equalsIgnoreCase("Others")) {

            return String.format(
                    "Your %s spending is working overtime. 💸",
                    topCategory
            );
        }

        return "You're spending money faster than your phone battery drains. 💸";
    }


    // =========================================================
    // MERCHANT ANALYTICS
    // =========================================================

    public List<Object[]> getMerchantSummary() {

        User user =
                getLoggedInUser();

        return transactionRepository
                .getMerchantSummary(user);
    }


    // =========================================================
    // CATEGORY ANALYTICS
    // =========================================================

    public List<Object[]> getCategorySummary() {

        User user =
                getLoggedInUser();

        return transactionRepository
                .getCategorySummary(user);
    }


    // =========================================================
    // ACCOUNT-WISE ANALYTICS
    // =========================================================

    public List<Map<String, Object>> getAccountWiseAnalytics() {

        User user =
                getLoggedInUser();

        List<Account> accounts =
                accountRepository.findByUser(user);

        List<Object[]> summaries =
                transactionRepository
                        .getAccountTransactionSummary(user);

        List<Map<String, Object>> result =
                new ArrayList<>();


        for (Account account : accounts) {

            double income = 0.0;
            double expense = 0.0;
            double withdrawal = 0.0;


            for (Object[] row : summaries) {

                Long accountId =
                        ((Number) row[0]).longValue();

                String type =
                        row[1] == null
                                ? ""
                                : row[1].toString();

                double amount =
                        row[2] == null
                                ? 0.0
                                : ((Number) row[2]).doubleValue();


                if (!account.getId().equals(accountId)) {
                    continue;
                }


                if ("INCOME".equalsIgnoreCase(type)) {
                    income += amount;
                }

                if ("EXPENSE".equalsIgnoreCase(type)) {
                    expense += amount;
                }

                if ("WITHDRAWAL".equalsIgnoreCase(type)) {
                    withdrawal += amount;
                }
            }


            Map<String, Object> accountData =
                    new LinkedHashMap<>();

            accountData.put(
                    "accountId",
                    account.getId()
            );

            accountData.put(
                    "accountName",
                    account.getAccountName()
            );

            accountData.put(
                    "bankName",
                    account.getBankName()
            );

            accountData.put(
                    "accountType",
                    account.getAccountType()
            );

            accountData.put(
                    "currentBalance",
                    account.getCurrentBalance()
            );

            accountData.put(
                    "income",
                    income
            );

            accountData.put(
                    "expense",
                    expense
            );

            accountData.put(
                    "withdrawal",
                    withdrawal
            );

            result.add(accountData);
        }


        return result;
    }


    // =========================================================
    // ACCOUNT CATEGORY SUMMARY
    // =========================================================

    public List<Map<String, Object>> getAccountCategorySummary() {

        User user =
                getLoggedInUser();

        List<Account> accounts =
                accountRepository.findByUser(user);

        List<Object[]> summaries =
                transactionRepository
                        .getAccountCategorySummary(user);

        List<Map<String, Object>> result =
                new ArrayList<>();


        for (Account account : accounts) {

            for (Object[] row : summaries) {

                Long accountId =
                        ((Number) row[0]).longValue();

                if (!account.getId().equals(accountId)) {
                    continue;
                }


                String categoryName =
                        row[1] == null
                                ? "Others"
                                : row[1].toString();

                double amount =
                        row[2] == null
                                ? 0.0
                                : ((Number) row[2]).doubleValue();


                Map<String, Object> categoryData =
                        new LinkedHashMap<>();

                categoryData.put(
                        "accountId",
                        account.getId()
                );

                categoryData.put(
                        "accountName",
                        account.getAccountName()
                );

                categoryData.put(
                        "bankName",
                        account.getBankName()
                );

                categoryData.put(
                        "category",
                        categoryName
                );

                categoryData.put(
                        "amount",
                        amount
                );

                result.add(categoryData);
            }
        }


        return result;
    }


    // =========================================================
    // ACCOUNT MONTHLY EXPENSE SUMMARY
    // =========================================================

    public List<Map<String, Object>> getAccountMonthlyExpenseSummary() {

        User user =
                getLoggedInUser();

        List<Object[]> summaries =
                transactionRepository
                        .getAccountMonthlyExpenseSummary(user);

        List<Map<String, Object>> result =
                new ArrayList<>();


        for (Object[] row : summaries) {

            Long accountId =
                    ((Number) row[0]).longValue();

            String month =
                    row[1] == null
                            ? "Unknown"
                            : row[1].toString();

            double amount =
                    row[3] == null
                            ? 0.0
                            : ((Number) row[3]).doubleValue();


            Map<String, Object> monthlyData =
                    new LinkedHashMap<>();

            monthlyData.put(
                    "accountId",
                    accountId
            );

            monthlyData.put(
                    "month",
                    month
            );

            monthlyData.put(
                    "amount",
                    amount
            );

            result.add(monthlyData);
        }


        return result;
    }
}