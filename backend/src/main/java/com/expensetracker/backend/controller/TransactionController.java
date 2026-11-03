package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ManualTransactionRequest;
import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin("*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ==============================
    // GET ALL TRANSACTIONS
    // ==============================

    @GetMapping
    public List<Transaction> getTransactions() {
        return transactionService.getAllTransactions();
    }

    // ==============================
    // CREATE MANUAL TRANSACTION
    // ==============================

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody ManualTransactionRequest request) {

        Transaction transaction =
                transactionService.createManualTransaction(request);

        return ResponseEntity.ok(transaction);
    }
}