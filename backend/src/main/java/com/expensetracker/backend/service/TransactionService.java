package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AiCategorizationResponse;
import com.expensetracker.backend.dto.ManualTransactionRequest;
import com.expensetracker.backend.dto.ParsedNotification;
import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.Category;
import com.expensetracker.backend.entity.Merchant;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.entity.User;
import com.expensetracker.backend.repository.AccountRepository;
import com.expensetracker.backend.repository.CategoryRepository;
import com.expensetracker.backend.repository.MerchantRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AiCategorizationService aiCategorizationService;

    @Autowired
    private WhatsAppService whatsAppService;


    // =========================================================
    // GET LOGGED-IN USER
    // =========================================================

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No user found"));
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("User not found: " + email)));
    }


    // =========================================================
    // FIND CASH WALLET
    // =========================================================

    private Account findCashWallet(User user) {

        List<Account> userAccounts =
                accountRepository.findByUser(user);

        return userAccounts.stream()
                .filter(account -> {

                    String accountType =
                            account.getAccountType() == null
                                    ? ""
                                    : account.getAccountType().trim();

                    String accountName =
                            account.getAccountName() == null
                                    ? ""
                                    : account.getAccountName().trim();

                    String bankName =
                            account.getBankName() == null
                                    ? ""
                                    : account.getBankName().trim();

                    return
                            "CASH".equalsIgnoreCase(accountType)
                            || "PHYSICAL CASH".equalsIgnoreCase(accountType)
                            || "CASH".equalsIgnoreCase(accountName)
                            || "CASH WALLET".equalsIgnoreCase(accountName)
                            || "CASH".equalsIgnoreCase(bankName);
                })
                .findFirst()
                .orElse(null);
    }


    // =========================================================
    // CREATE NORMAL TRANSACTION
    // =========================================================

    @Transactional
    public Transaction createTransaction(Transaction transaction) {

        User user = getLoggedInUser();

        transaction.setUser(user);

        if (transaction.getTransactionType() == null ||
                transaction.getTransactionType().isBlank()) {

            transaction.setTransactionType("EXPENSE");
        }

        if (transaction.getTransactionTime() == null) {

            transaction.setTransactionTime(
                    LocalDateTime.now()
            );
        }

        if (transaction.getPaymentMethod() == null ||
                transaction.getPaymentMethod().isBlank()) {

            transaction.setPaymentMethod("OTHER");
        }


        // -------------------------
        // MERCHANT
        // -------------------------

        if (transaction.getMerchant() != null &&
                transaction.getMerchant().getName() != null &&
                !transaction.getMerchant().getName().isBlank()) {

            String merchantName =
                    transaction.getMerchant()
                            .getName()
                            .trim();

            final String finalMerchantName =
                    merchantName;

            Merchant merchant =
                    merchantRepository
                            .findByNameIgnoreCase(
                                    finalMerchantName
                            )
                            .orElseGet(() -> {

                                Merchant newMerchant =
                                        Merchant.builder()
                                                .name(finalMerchantName)
                                                .verified(false)
                                                .merchantType("UNKNOWN")
                                                .build();

                                return merchantRepository
                                        .save(newMerchant);
                            });

            transaction.setMerchant(merchant);
        }


        // -------------------------
        // CATEGORY
        // -------------------------

        if (transaction.getCategory() != null &&
                transaction.getCategory().getName() != null &&
                !transaction.getCategory().getName().isBlank()) {

            String categoryName =
                    transaction.getCategory()
                            .getName()
                            .trim();

            final String finalCategoryName =
                    categoryName;

            Category category =
                    categoryRepository
                            .findByNameIgnoreCase(
                                    finalCategoryName
                            )
                            .orElseGet(() -> {

                                Category newCategory =
                                        Category.builder()
                                                .name(finalCategoryName)
                                                .build();

                                return categoryRepository
                                        .save(newCategory);
                            });

            transaction.setCategory(category);
        }


        // -------------------------
        // ACCOUNT
        // -------------------------

        Account account =
                transaction.getAccount();

        if (account == null) {

            account =
                    accountRepository
                            .findByUserAndPrimaryAccountTrue(user)
                            .orElse(null);

            transaction.setAccount(account);
        }


        Transaction savedTransaction =
                transactionRepository.save(transaction);


        // -------------------------
        // UPDATE BALANCE
        // -------------------------

        if (account != null &&
                account.getCurrentBalance() != null &&
                transaction.getAmount() != null) {

            double amount =
                    transaction.getAmount();

            if ("INCOME".equalsIgnoreCase(
                    transaction.getTransactionType())) {

                account.setCurrentBalance(
                        account.getCurrentBalance()
                                + amount
                );

            } else if ("EXPENSE".equalsIgnoreCase(
                    transaction.getTransactionType())) {

                account.setCurrentBalance(
                        account.getCurrentBalance()
                                - amount
                );

            }

            accountRepository.save(account);
        }

        return savedTransaction;
    }


    // =========================================================
    // SAVE AI PARSED TRANSACTION
    // =========================================================

    @Transactional
    public void saveTransaction(
            ParsedNotification parsed,
            String notificationText) {

        User user = getLoggedInUser();

        Account account =
                accountRepository
                        .findByUserAndPrimaryAccountTrue(user)
                        .orElse(null);

        Transaction transaction = new Transaction();
        transaction.setUser(user);

        // Merchant
        String merchantName = (parsed.getMerchant() == null || parsed.getMerchant().isBlank())
                ? "Unknown" : parsed.getMerchant().trim();
        final String finalMerchantName = merchantName;

        Merchant merchant = merchantRepository
                .findByNameIgnoreCase(finalMerchantName)
                .orElseGet(() -> merchantRepository.save(
                        Merchant.builder()
                                .name(finalMerchantName)
                                .verified(false)
                                .merchantType("UNKNOWN")
                                .build()
                ));
        transaction.setMerchant(merchant);
        transaction.setAmount(parsed.getAmount());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setPaymentMethod(parsed.getPaymentMethod());
        transaction.setNotificationText(notificationText);

        String txnType = (parsed.getTransactionType() != null && !parsed.getTransactionType().isBlank())
                ? parsed.getTransactionType()
                : "EXPENSE";
        transaction.setTransactionType(txnType);

        // AI Category
        try {
            if ("WITHDRAWAL".equalsIgnoreCase(txnType)) {
                Category cat = categoryRepository.findByNameIgnoreCase("Cash Transfer")
                        .orElseGet(() -> categoryRepository.save(Category.builder().name("Cash Transfer").build()));
                transaction.setCategory(cat);
                transaction.setAiCategory("Cash & ATM");
                transaction.setAiConfidence(1.0);
                transaction.setAiReasoning("ATM Cash Withdrawal to Cash Wallet");
            } else {
                AiCategorizationResponse aiResponse = aiCategorizationService.categorize(notificationText);
                Category category = categoryRepository
                        .findByNameIgnoreCase(aiResponse.getCategory())
                        .orElseGet(() -> categoryRepository.save(Category.builder().name(aiResponse.getCategory()).build()));

                transaction.setCategory(category);
                transaction.setAiCategory(aiResponse.getCategory());
                transaction.setAiConfidence(aiResponse.getConfidence());
                transaction.setAiReasoning(aiResponse.getReasoning());
            }
        } catch (Exception e) {
            Category otherCategory = categoryRepository.findByNameIgnoreCase("Others")
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Others").build()));
            transaction.setCategory(otherCategory);
            transaction.setAiCategory("Others");
            transaction.setAiConfidence(0.0);
            transaction.setAiReasoning("AI categorization fallback");
        }

        // ==========================================
        // LIQUID CASH & WITHDRAWAL BALANCE MANAGEMENT
        // ==========================================
        if ("WITHDRAWAL".equalsIgnoreCase(txnType)) {
            // ATM Withdrawal: Bank Account -> Cash Wallet
            Account cashWallet = findCashWallet(user);
            if (cashWallet == null) {
                cashWallet = Account.builder()
                        .accountName("Cash Wallet")
                        .accountType("CASH")
                        .bankName("Physical Cash")
                        .currentBalance(0.0)
                        .primaryAccount(false)
                        .user(user)
                        .build();
                cashWallet = accountRepository.save(cashWallet);
            }

            if (account != null && account.getCurrentBalance() != null && parsed.getAmount() != null) {
                account.setCurrentBalance(account.getCurrentBalance() - parsed.getAmount());
                accountRepository.save(account);
            }

            if (cashWallet.getCurrentBalance() == null) cashWallet.setCurrentBalance(0.0);
            if (parsed.getAmount() != null) {
                cashWallet.setCurrentBalance(cashWallet.getCurrentBalance() + parsed.getAmount());
            }
            accountRepository.save(cashWallet);
            transaction.setAccount(account);

        } else if ("CASH".equalsIgnoreCase(parsed.getPaymentMethod())) {
            // Physical Cash Spend: Deduct from Cash Wallet
            Account cashWallet = findCashWallet(user);
            if (cashWallet == null) {
                cashWallet = Account.builder()
                        .accountName("Cash Wallet")
                        .accountType("CASH")
                        .bankName("Physical Cash")
                        .currentBalance(0.0)
                        .primaryAccount(false)
                        .user(user)
                        .build();
                cashWallet = accountRepository.save(cashWallet);
            }

            if (cashWallet.getCurrentBalance() != null && parsed.getAmount() != null) {
                cashWallet.setCurrentBalance(Math.max(0.0, cashWallet.getCurrentBalance() - parsed.getAmount()));
                accountRepository.save(cashWallet);
            }
            transaction.setAccount(cashWallet);

        } else if ("INCOME".equalsIgnoreCase(txnType)) {
            // Autonomous Income/Credit: Salary, UPI Received, Cashback, Refunds
            if (account != null && account.getCurrentBalance() != null && parsed.getAmount() != null) {
                account.setCurrentBalance(account.getCurrentBalance() + parsed.getAmount());
                accountRepository.save(account);
            }
            transaction.setAccount(account);

        } else {
            // Normal Digital/UPI/Card Bank Expense
            if (account != null && account.getCurrentBalance() != null && parsed.getAmount() != null) {
                account.setCurrentBalance(account.getCurrentBalance() - parsed.getAmount());
                accountRepository.save(account);
            }
            transaction.setAccount(account);
        }

        Transaction savedTxn = transactionRepository.save(transaction);

        // Trigger WhatsApp Guilt-Trip Roast if food spending threshold met (e.g. 4th biryani)
        try {
            whatsAppService.checkAndInterveneSpending(user, savedTxn);
        } catch (Exception e) {
            System.out.println("WhatsApp roast check skipped: " + e.getMessage());
        }
    }


    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    public List<Transaction> getAllTransactions() {

        User user = getLoggedInUser();

        return transactionRepository
                .findByUserOrderByTransactionTimeDesc(user);
    }


    // =========================================================
    // CREATE MANUAL TRANSACTION
    // =========================================================

    @Transactional
    public Transaction createManualTransaction(
            ManualTransactionRequest request) {

        User user = getLoggedInUser();


        // =====================================================
        // VALIDATE AMOUNT
        // =====================================================

        if (request.getAmount() == null ||
                request.getAmount() <= 0) {

            throw new RuntimeException(
                    "Transaction amount must be greater than 0"
            );
        }


        // =====================================================
        // TRANSACTION TYPE
        // =====================================================

        String transactionType =
                request.getTransactionType();

        if (transactionType == null ||
                transactionType.isBlank()) {

            transactionType = "EXPENSE";
        }

        transactionType =
                transactionType.toUpperCase();


        // =====================================================
        // PAYMENT METHOD
        // =====================================================

        String paymentMethod =
                request.getPaymentMethod();

        if (paymentMethod == null ||
                paymentMethod.isBlank()) {

            paymentMethod = "OTHER";
        }

        paymentMethod =
                paymentMethod.toUpperCase();


        // =====================================================
        // VALIDATE TYPE
        // =====================================================

        if (!"EXPENSE".equals(transactionType) &&
                !"INCOME".equals(transactionType) &&
                !"WITHDRAWAL".equals(transactionType)) {

            throw new RuntimeException(
                    "Invalid transaction type. " +
                    "Use EXPENSE, INCOME or WITHDRAWAL."
            );
        }


        // =====================================================
        // FIND SELECTED ACCOUNT
        // =====================================================

        Account account;

        if (request.getAccountId() != null) {

            account =
                    accountRepository
                            .findById(request.getAccountId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Selected account not found."
                                    ));

            // Make sure account belongs to logged-in user
            if (account.getUser() == null ||
                    !account.getUser().getId()
                            .equals(user.getId())) {

                throw new RuntimeException(
                        "You cannot use another user's account."
                );
            }

        } else {

            // Backward compatibility
            account =
                    accountRepository
                            .findByUserAndPrimaryAccountTrue(user)
                            .orElse(null);

            if (account == null) {

                throw new RuntimeException(
                        "Primary account not found."
                );
            }
        }


        // =====================================================
        // CASH PAYMENT
        // =====================================================

        if ("CASH".equalsIgnoreCase(paymentMethod)) {

            Account cashWallet =
                    findCashWallet(user);

            if (cashWallet == null) {

                throw new RuntimeException(
                        "Cash Wallet not found."
                );
            }

            account = cashWallet;
        }


        // =====================================================
        // WITHDRAWAL
        // =====================================================

        if ("WITHDRAWAL".equals(transactionType)) {

            if ("CASH".equalsIgnoreCase(
                    account.getAccountType())
                    ||
                    "PHYSICAL CASH".equalsIgnoreCase(
                            account.getAccountType())) {

                throw new RuntimeException(
                        "Cash Wallet cannot be used " +
                        "as the source of a withdrawal."
                );
            }


            if (account.getCurrentBalance() == null ||
                    account.getCurrentBalance()
                            < request.getAmount()) {

                throw new RuntimeException(
                        "Insufficient balance in selected account."
                );
            }
        }


        // =====================================================
        // MERCHANT
        // =====================================================

        String merchantName =
                request.getMerchant();

        if (merchantName == null ||
                merchantName.isBlank()) {

            if ("WITHDRAWAL".equals(transactionType)) {

                merchantName =
                        "ATM Withdrawal";

            } else {

                merchantName =
                        "Unknown";
            }
        }


        final String finalMerchantName =
                merchantName.trim();


        Merchant merchant =
                merchantRepository
                        .findByNameIgnoreCase(
                                finalMerchantName
                        )
                        .orElseGet(() -> {

                            Merchant newMerchant =
                                    Merchant.builder()
                                            .name(finalMerchantName)
                                            .verified(false)
                                            .merchantType("UNKNOWN")
                                            .build();

                            return merchantRepository
                                    .save(newMerchant);
                        });


        // =====================================================
        // CATEGORY
        // =====================================================

        String categoryName =
                request.getCategory();

        if (categoryName == null ||
                categoryName.isBlank()) {

            if ("WITHDRAWAL".equals(transactionType)) {

                categoryName =
                        "Cash Withdrawal";

            } else {

                categoryName =
                        "Others";
            }
        }


        final String finalCategoryName =
                categoryName.trim();


        Category category =
                categoryRepository
                        .findByNameIgnoreCase(
                                finalCategoryName
                        )
                        .orElseGet(() -> {

                            Category newCategory =
                                    Category.builder()
                                            .name(finalCategoryName)
                                            .build();

                            return categoryRepository
                                    .save(newCategory);
                        });


        // =====================================================
        // CREATE TRANSACTION
        // =====================================================

        Transaction transaction =
                new Transaction();

        transaction.setUser(user);

        transaction.setAccount(account);

        transaction.setAmount(
                request.getAmount()
        );

        transaction.setTransactionTime(
                LocalDateTime.now()
        );

        transaction.setPaymentMethod(
                paymentMethod
        );

        transaction.setTransactionType(
                transactionType
        );

        transaction.setNotificationText(
                request.getNotificationText()
        );

        transaction.setMerchant(
                merchant
        );

        transaction.setCategory(
                category
        );

        transaction.setAiCategory(
                finalCategoryName
        );

        transaction.setAiConfidence(
                1.0
        );

        transaction.setAiReasoning(
                "Manually added transaction."
        );


        // =====================================================
        // CASH EXPENSE VALIDATION
        // =====================================================

        if ("EXPENSE".equals(transactionType)
                &&
                "CASH".equalsIgnoreCase(
                        paymentMethod)) {

            if (account.getCurrentBalance() == null ||
                    account.getCurrentBalance()
                            < request.getAmount()) {

                throw new RuntimeException(
                        "Insufficient Cash Wallet balance."
                );
            }
        }


        // =====================================================
        // CASH INCOME
        // =====================================================

        if ("INCOME".equals(transactionType)
                &&
                "CASH".equalsIgnoreCase(
                        paymentMethod)) {

            if (account.getCurrentBalance() == null) {

                account.setCurrentBalance(0.0);
            }

            account.setCurrentBalance(
                    account.getCurrentBalance()
                            + request.getAmount()
            );

            accountRepository.save(account);

            return transactionRepository.save(
                    transaction
            );
        }


        // =====================================================
        // CASH EXPENSE
        // =====================================================

        if ("EXPENSE".equals(transactionType)) {

            account.setCurrentBalance(
                    account.getCurrentBalance()
                            - request.getAmount()
            );

            accountRepository.save(account);

            return transactionRepository.save(
                    transaction
            );
        }


        // =====================================================
        // NORMAL INCOME
        // =====================================================

        if ("INCOME".equals(transactionType)) {

            account.setCurrentBalance(
                    account.getCurrentBalance()
                            + request.getAmount()
            );

            accountRepository.save(account);

            return transactionRepository.save(
                    transaction
            );
        }


        // =====================================================
        // WITHDRAWAL: BANK → CASH WALLET
        // =====================================================

        if ("WITHDRAWAL".equals(transactionType)) {

            Account cashWallet =
                    findCashWallet(user);

            if (cashWallet == null) {

                throw new RuntimeException(
                        "Cash Wallet not found. " +
                        "Please create a Cash account first."
                );
            }


            if (cashWallet.getId()
                    .equals(account.getId())) {

                throw new RuntimeException(
                        "Source account and Cash Wallet " +
                        "cannot be the same."
                );
            }


            if (cashWallet.getCurrentBalance() == null) {

                cashWallet.setCurrentBalance(
                        0.0
                );
            }


            // Add money to Cash Wallet
            cashWallet.setCurrentBalance(
                    cashWallet.getCurrentBalance()
                            + request.getAmount()
            );


            // Remove money from selected bank account
            account.setCurrentBalance(
                    account.getCurrentBalance()
                            - request.getAmount()
            );


            accountRepository.save(account);

            accountRepository.save(cashWallet);


            return transactionRepository.save(
                    transaction
            );
        }


        return transactionRepository.save(
                transaction
        );
    }
}