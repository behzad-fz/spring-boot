package com.bank.modules.transaction.controller;

import com.bank.modules.transaction.entity.CurrencyConversionResult;
import com.bank.modules.transaction.entity.ScheduledTransaction;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.entity.TransferResult;
import com.bank.modules.transaction.enums.TransactionType;
import com.bank.modules.transaction.request.CurrencyConversionRequest;
import com.bank.modules.transaction.request.NewTransaction;
import com.bank.modules.transaction.request.RecipientPaymentRequest;
import com.bank.modules.transaction.request.ScheduleTransactionRequest;
import com.bank.modules.transaction.request.TransferRequest;
import com.bank.modules.transaction.service.TransactionService;
import com.bank.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/accounts/{accountUUID}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    private ResponseEntity<PageResponse<Transaction>> getTransactions(
            @PathVariable String accountUUID,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Transaction> transactions = transactionService.getTransactions(
                accountUUID, from, to, type, minAmount, maxAmount,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "initiatedAt")));

        return ResponseEntity.ok(PageResponse.from(transactions));
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

    @PostMapping("/scheduled")
    private ResponseEntity<ScheduledTransaction> scheduleTransaction(
        @Valid @RequestBody ScheduleTransactionRequest request,
        @PathVariable String accountUUID
    ) {
        ScheduledTransaction scheduled = transactionService.scheduleTransaction(accountUUID, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(scheduled);
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
