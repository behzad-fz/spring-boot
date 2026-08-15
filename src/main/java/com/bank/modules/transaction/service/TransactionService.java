package com.bank.modules.transaction.service;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.RecipientNotFoundException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.enums.TransactionType;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.repository.TransactionRepository;
import com.bank.modules.transaction.request.NewTransaction;
import com.bank.modules.transaction.request.RecipientPaymentRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              RecipientRepository recipientRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.recipientRepository = recipientRepository;
    }

    @Transactional
    public Transaction createTransaction(NewTransaction newTransaction, String accountUUID) {
        TransactionType type = TransactionType.valueOf(newTransaction.getTransactionType());
        BigDecimal amount = newTransaction.getAmount();

        Account account = requireAccount(accountUUID);

        BigDecimal signedAmount = signedAmount(type, amount);
        BigDecimal newBalance = account.getBalance().add(signedAmount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient funds for this transaction");
        }

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(type)
                .description(newTransaction.getDescription())
                .build();

        transaction.setAccount(account);

        Transaction persistedTransaction = transactionRepository.save(transaction);

        account.setBalance(newBalance);
        account.setLastTransaction(LocalDate.now());
        accountRepository.save(account);

        persistedTransaction.setStatus(TransactionStatus.COMPLETED);
        persistedTransaction.setCompletedAt(LocalDateTime.now());
        return transactionRepository.save(persistedTransaction);
    }

    @Transactional
    public Transaction payToRecipient(String accountUUID, RecipientPaymentRequest request) {
        Account account = requireAccount(accountUUID);
        Recipient recipient = recipientRepository.findByIban(request.getRecipientIban());

        if (recipient == null || !isRecipientOwnedByCustomer(recipient, account)) {
            throw new RecipientNotFoundException("Recipient not found or not owned by this customer");
        }

        return createTransaction(NewTransaction.builder()
                .amount(request.getAmount())
                .transactionType("PAYMENT")
                .description(request.getDescription())
                .build(), accountUUID);
    }

    private Account requireAccount(String accountUUID) {
        Account account = accountRepository.findByUUID(accountUUID);

        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        requireOwnership(account);

        return account;
    }

    private void requireOwnership(Account account) {
        Customer authenticatedCustomer = authenticatedCustomer();
        if (authenticatedCustomer != null
                && (account.getCustomer() == null
                    || !account.getCustomer().getUUID().equals(authenticatedCustomer.getUUID()))) {
            throw new AccessDeniedException("Account does not belong to the authenticated customer");
        }
    }

    private Customer authenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Customer customer) {
            return customer;
        }
        return null;
    }

    private boolean isRecipientOwnedByCustomer(Recipient recipient, Account account) {
        return account.getCustomer() != null
                && recipient.getCustomer() != null
                && recipient.getCustomer().getUUID().equals(account.getCustomer().getUUID());
    }

    private BigDecimal signedAmount(TransactionType type, BigDecimal amount) {
        return switch (type) {
            case DEPOSIT -> amount;
            case WITHDRAWAL, PAYMENT, TRANSFER -> amount.negate();
            case CURRENCY_CONVERSION -> amount;
        };
    }
}
