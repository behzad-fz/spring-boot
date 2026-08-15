package com.bank.modules.transaction.service;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.RecipientNotFoundException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.transaction.entity.CurrencyConversionResult;
import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.entity.ScheduledTransaction;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.entity.TransferResult;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.enums.TransactionType;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.repository.ScheduledTransactionRepository;
import com.bank.modules.transaction.repository.TransactionRepository;
import com.bank.modules.transaction.request.CurrencyConversionRequest;
import com.bank.modules.transaction.request.NewTransaction;
import com.bank.modules.transaction.request.RecipientPaymentRequest;
import com.bank.modules.transaction.request.ScheduleTransactionRequest;
import com.bank.modules.transaction.request.TransferRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;
    private final ScheduledTransactionRepository scheduledTransactionRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              RecipientRepository recipientRepository,
                              ScheduledTransactionRepository scheduledTransactionRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.recipientRepository = recipientRepository;
        this.scheduledTransactionRepository = scheduledTransactionRepository;
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
    public List<Transaction> getTransactions(String accountUUID) {
        Account account = requireAccount(accountUUID);

        return transactionRepository.findByAccountUUIDOrderByInitiatedAtDesc(account.getUUID());
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

    @Transactional
    public CurrencyConversionResult convertCurrency(String sourceAccountUUID, CurrencyConversionRequest request) {        Account source = requireAccount(sourceAccountUUID);
        Account target = requireAccount(request.getTargetAccountUUID());

        if (source.getUUID().equals(target.getUUID())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        BigDecimal targetAmount = request.getAmount().multiply(request.getExchangeRate());

        Transaction sourceLeg = persistLeg(source, TransactionType.CURRENCY_CONVERSION, request.getAmount().negate(),
                "Currency conversion", source.getBalance().subtract(request.getAmount()));
        Transaction targetLeg = persistLeg(target, TransactionType.CURRENCY_CONVERSION, targetAmount,
                "Currency conversion", target.getBalance().add(targetAmount));

        return new CurrencyConversionResult(sourceLeg, targetLeg);
    }

    @Transactional
    public ScheduledTransaction scheduleTransaction(String accountUUID, ScheduleTransactionRequest request) {
        Account account = requireAccount(accountUUID);

        ScheduledTransaction scheduled = ScheduledTransaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionType(TransactionType.WITHDRAWAL)
                .runAt(request.getRunAt())
                .status(TransactionStatus.PENDING)
                .account(account)
                .build();

        return scheduledTransactionRepository.save(scheduled);
    }

    @Transactional
    public List<ScheduledTransaction> processDueScheduledTransactions() {
        List<ScheduledTransaction> due = scheduledTransactionRepository
                .findByStatusAndRunAtLessThanEqual(TransactionStatus.PENDING, LocalDateTime.now());

        for (ScheduledTransaction scheduled : due) {
            try {
                createTransaction(NewTransaction.builder()
                        .amount(scheduled.getAmount())
                        .transactionType("WITHDRAWAL")
                        .description(scheduled.getDescription())
                        .build(), scheduled.getAccount().getUUID());

                scheduled.setStatus(TransactionStatus.COMPLETED);
                scheduled.setProcessedAt(LocalDateTime.now());
            } catch (InsufficientFundsException ex) {
                scheduled.setStatus(TransactionStatus.FAILED);
                scheduled.setProcessedAt(LocalDateTime.now());
                scheduled.setStatusExplanation(ex.getMessage());
            }
            scheduledTransactionRepository.save(scheduled);
        }

        return due;
    }

    @Transactional
    public TransferResult transferBetweenAccounts(String sourceAccountUUID, TransferRequest request) {
        Account source = requireAccount(sourceAccountUUID);
        Account target = requireAccount(request.getTargetAccountUUID());

        if (source.getUUID().equals(target.getUUID())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        if (source.getCurrency() != target.getCurrency()) {
            throw new IllegalArgumentException("Transfer requires accounts with the same currency; use currency conversion for different currencies");
        }

        Transaction sourceLeg = persistLeg(source, TransactionType.TRANSFER, request.getAmount().negate(),
                "Transfer", source.getBalance().subtract(request.getAmount()));
        Transaction targetLeg = persistLeg(target, TransactionType.TRANSFER, request.getAmount(),
                "Transfer", target.getBalance().add(request.getAmount()));

        return new TransferResult(sourceLeg, targetLeg);
    }

    private Transaction persistLeg(Account account, TransactionType type, BigDecimal signedAmount, String description, BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient funds for this transaction");
        }

        Transaction transaction = Transaction.builder()
                .amount(signedAmount.abs())
                .transactionType(type)
                .description(description)
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
