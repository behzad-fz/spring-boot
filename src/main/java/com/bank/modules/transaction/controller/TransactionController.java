package com.bank.modules.transaction.controller;

import com.bank.modules.transaction.entity.CurrencyConversionResult;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.entity.TransferResult;
import com.bank.modules.transaction.request.CurrencyConversionRequest;
import com.bank.modules.transaction.request.NewTransaction;
import com.bank.modules.transaction.request.RecipientPaymentRequest;
import com.bank.modules.transaction.request.TransferRequest;
import com.bank.modules.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/accounts/{accountUUID}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    private ResponseEntity<List<Transaction>> getTransactions(@PathVariable String accountUUID) {
        List<Transaction> transactions = transactionService.getTransactions(accountUUID);

        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    private ResponseEntity<Transaction> createNewTransaction(
        @Valid @RequestBody NewTransaction newTransaction,
        @PathVariable String accountUUID
    ) {
        Transaction transaction = transactionService.createTransaction(newTransaction, accountUUID);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/recipient-payment")
    private ResponseEntity<Transaction> payToRecipient(
        @Valid @RequestBody RecipientPaymentRequest request,
        @PathVariable String accountUUID
    ) {
        Transaction transaction = transactionService.payToRecipient(accountUUID, request);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/currency-conversion")
    private ResponseEntity<CurrencyConversionResult> convertCurrency(
        @Valid @RequestBody CurrencyConversionRequest request,
        @PathVariable String accountUUID
    ) {
        CurrencyConversionResult result = transactionService.convertCurrency(accountUUID, request);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/transfer")
    private ResponseEntity<TransferResult> transfer(
        @Valid @RequestBody TransferRequest request,
        @PathVariable String accountUUID
    ) {
        TransferResult result = transactionService.transferBetweenAccounts(accountUUID, request);

        return ResponseEntity.ok(result);
    }
}
