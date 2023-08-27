package com.bank.modules.transaction.controller;

import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.request.NewTransaction;
import com.bank.modules.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/accounts/{accountUUID}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    private ResponseEntity<Transaction> createNewTransaction(
        @Valid @RequestBody NewTransaction newTransaction,
        @PathVariable String accountUUID
    ) {
        Transaction transaction = transactionService.createTransaction(newTransaction, accountUUID);

        return ResponseEntity.ok(transaction);
    }
}
