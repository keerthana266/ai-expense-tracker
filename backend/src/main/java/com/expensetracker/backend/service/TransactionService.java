package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.ParsedNotification;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.expensetracker.backend.dto.AiCategorizationResponse;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AiCategorizationService aiCategorizationService;
    public void saveTransaction(ParsedNotification parsed) {

        Transaction transaction = new Transaction();

        transaction.setMerchant(parsed.getMerchant());
        transaction.setAmount(parsed.getAmount());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setPaymentMethod(parsed.getPaymentMethod());

        // Temporary values until we build the AI module
        AiCategorizationResponse aiResponse =
                aiCategorizationService.categorize(parsed.getMerchant());

        transaction.setTransactionType("EXPENSE");
        transaction.setAiCategory(aiResponse.getCategory());
        transaction.setAiConfidence(aiResponse.getConfidence());

        System.out.println("========== AI ANALYSIS ==========");
        System.out.println("AI Category  : " + aiResponse.getCategory());
        System.out.println("Confidence   : " + aiResponse.getConfidence());
        System.out.println("Reason       : " + aiResponse.getReasoning());
        System.out.println("=================================");

        // Store the raw notification later when we integrate everything
        transaction.setNotificationText("");

        // User, Account and Category will be linked in the next phase

        transactionRepository.save(transaction);
    }
}