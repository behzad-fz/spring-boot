package com.bank.modules.transaction.service;

import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.repository.TransactionRepository;
import com.bank.modules.transaction.request.NewTransaction;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Transaction createTransaction(NewTransaction newTransaction, String accountUUID) {

        Transaction transaction = Transaction.builder()
                .amount(newTransaction.getAmount())
                .transactionType(newTransaction.getTransactionType())
                .description(newTransaction.getDescription())
                .build();

        Account account = accountRepository.findByUUID(accountUUID);

        transaction.setAccount(account);

        Transaction persistedTransaction =  transactionRepository.save(transaction);

        // Update account table accordingly
        account.setBalance(account.getBalance().add(persistedTransaction.getAmount()));
        account.setLastTransaction(LocalDate.now());
        accountRepository.save(account);

        persistedTransaction.setStatus(TransactionStatus.COMPLETED);
        persistedTransaction.setCompletedAt(LocalDateTime.now());
        return transactionRepository.save(persistedTransaction);
    }
}
