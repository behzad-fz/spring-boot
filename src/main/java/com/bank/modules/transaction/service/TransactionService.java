package com.bank.modules.transaction.service;

import com.bank.exception.AccountNotOperableException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.RecipientNotFoundException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.transaction.service.ExchangeRateService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;
    private final ScheduledTransactionRepository scheduledTransactionRepository;
    private final ExchangeRateService exchangeRateService;

    private static final Set<AccountStatus> BLOCKED_STATUSES = EnumSet.of(
            AccountStatus.CLOSED, AccountStatus.SUSPENDED, AccountStatus.UNDER_INVESTIGATION);

    @Autowired
    private ApplicationContext applicationContext;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              RecipientRepository recipientRepository,
                              ScheduledTransactionRepository scheduledTransactionRepository,
                              ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.recipientRepository = recipientRepository;
        this.scheduledTransactionRepository = scheduledTransactionRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public Transaction createTransaction(NewTransaction newTransaction, String accountUUID) {
        TransactionType type = TransactionType.valueOf(newTransaction.getTransactionType());
        BigDecimal amount = newTransaction.getAmount();

        Account account = requireAccount(accountUUID);
        requireOperable(account);

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

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactions(String accountUUID, LocalDateTime from, LocalDateTime to,
                                             TransactionType type, BigDecimal minAmount, BigDecimal maxAmount,
                                             Pageable pageable) {
        Account account = requireAccount(accountUUID);

        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("account").get("UUID"), account.getUUID()));
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("initiatedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("initiatedAt"), to));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec, pageable);
    }

    @Transactional
    public Transaction payToRecipient(String accountUUID, RecipientPaymentRequest request) {
        Account account = requireAccount(accountUUID);
        Recipient recipient = recipientRepository.findByIban(request.getRecipientIban());

        if (recipient == null || !isRecipientOwnedByCustomer(recipient, account)) {
            throw new RecipientNotFoundException("Recipient not found or not owned by this customer");
        }

        Account target = accountRepository.findByIban(request.getRecipientIban());
        if (target == null) {
            throw new IllegalArgumentException("No internal account exists for recipient IBAN");
        }

        Account[] pair = requireAccountsLockedInDeterministicOrder(account.getUUID(), target.getUUID());
        Account source = pair[0];
        Account recipientAccount = pair[1];

        requireOperable(source);
        requireOperable(recipientAccount);

        Transaction sourceLeg = persistLeg(source, TransactionType.PAYMENT, request.getAmount().negate(),
                request.getDescription(), source.getBalance().subtract(request.getAmount()));
        persistLeg(recipientAccount, TransactionType.PAYMENT, request.getAmount(),
                request.getDescription(), recipientAccount.getBalance().add(request.getAmount()));

        return sourceLeg;
    }
    @Transactional
    public CurrencyConversionResult convertCurrency(String sourceAccountUUID, CurrencyConversionRequest request) {
        if (sourceAccountUUID.equals(request.getTargetAccountUUID())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        Account[] pair = requireAccountsLockedInDeterministicOrder(sourceAccountUUID, request.getTargetAccountUUID());
        Account source = pair[0];
        Account target = pair[1];

        requireOperable(source);
        requireOperable(target);

        BigDecimal targetAmount = request.getAmount().multiply(
                exchangeRateService.rate(source.getCurrency(), target.getCurrency()));

        Transaction sourceLeg = persistLeg(source, TransactionType.CURRENCY_CONVERSION, request.getAmount().negate(),
                "Currency conversion", source.getBalance().subtract(request.getAmount()));
        Transaction targetLeg = persistLeg(target, TransactionType.CURRENCY_CONVERSION, targetAmount,
                "Currency conversion", target.getBalance().add(targetAmount));

        return new CurrencyConversionResult(sourceLeg, targetLeg);
    }

    @Transactional
    public ScheduledTransaction scheduleTransaction(String accountUUID, ScheduleTransactionRequest request) {
        Account account = requireAccount(accountUUID);
        requireOperable(account);

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

        List<ScheduledTransaction> processed = new ArrayList<>();
        for (ScheduledTransaction scheduled : due) {
            try {
                proxy().processOneScheduled(scheduled);
            } catch (Exception ex) {
                scheduled.setStatus(TransactionStatus.FAILED);
                scheduled.setProcessedAt(LocalDateTime.now());
                scheduled.setStatusExplanation(ex.getMessage());
                scheduledTransactionRepository.save(scheduled);
            }
            processed.add(scheduled);
        }

        return processed;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOneScheduled(ScheduledTransaction scheduled) {
        createTransaction(NewTransaction.builder()
                .amount(scheduled.getAmount())
                .transactionType("WITHDRAWAL")
                .description(scheduled.getDescription())
                .build(), scheduled.getAccount().getUUID());

        scheduled.setStatus(TransactionStatus.COMPLETED);
        scheduled.setProcessedAt(LocalDateTime.now());
        scheduledTransactionRepository.save(scheduled);
    }

    private TransactionService proxy() {
        if (applicationContext != null) {
            return applicationContext.getBean(TransactionService.class);
        }
        return this;
    }

    @Transactional
    public TransferResult transferBetweenAccounts(String sourceAccountUUID, TransferRequest request) {
        if (sourceAccountUUID.equals(request.getTargetAccountUUID())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        Account[] pair = requireAccountsLockedInDeterministicOrder(sourceAccountUUID, request.getTargetAccountUUID());
        Account source = pair[0];
        Account target = pair[1];

        requireOperable(source);
        requireOperable(target);

        if (source.getCurrency() != target.getCurrency()) {
            throw new IllegalArgumentException("Transfer requires accounts with the same currency; use currency conversion for different currencies");
        }

        Transaction sourceLeg = persistLeg(source, TransactionType.TRANSFER, request.getAmount().negate(),
                "Transfer", source.getBalance().subtract(request.getAmount()));
        Transaction targetLeg = persistLeg(target, TransactionType.TRANSFER, request.getAmount(),
                "Transfer", target.getBalance().add(request.getAmount()));

        return new TransferResult(sourceLeg, targetLeg);
    }

    /**
     * Locks both accounts in deterministic (lexicographic UUID) order so that two
     * opposite-direction transfers cannot deadlock each other — every multi-account
     * operation acquires the row locks in the same global order.
     *
     * @return {source, target} in role order, both locked
     */
    private Account[] requireAccountsLockedInDeterministicOrder(String sourceUUID, String targetUUID) {
        boolean sourceIsLower = sourceUUID.compareTo(targetUUID) < 0;
        String firstUUID = sourceIsLower ? sourceUUID : targetUUID;
        String secondUUID = sourceIsLower ? targetUUID : sourceUUID;

        Account first = requireAccount(firstUUID);
        Account second = requireAccount(secondUUID);

        return sourceIsLower ? new Account[]{first, second} : new Account[]{second, first};
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
        Account account = accountRepository.findByUUIDForUpdate(accountUUID);

        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        requireOwnership(account);

        return account;
    }

    private void requireOwnership(Account account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            // No authenticated principal (e.g. server-side scheduled processing) — skip ownership.
            return;
        }

        if (authentication.getPrincipal() instanceof Customer customer) {
            if (account.getCustomer() == null
                    || !account.getCustomer().getUUID().equals(customer.getUUID())) {
                throw new AccessDeniedException("Account does not belong to the authenticated customer");
            }
            return;
        }

        // Non-customer principals (USER/admin) must not transact on customer accounts via this path.
        throw new AccessDeniedException("Only the account owner may perform transactions");
    }

    private void requireOperable(Account account) {
        if (BLOCKED_STATUSES.contains(account.getStatus())) {
            throw new AccountNotOperableException(
                    "Account is " + account.getStatus() + " and cannot perform transactions");
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
