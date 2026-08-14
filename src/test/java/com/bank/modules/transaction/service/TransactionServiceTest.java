package com.bank.modules.transaction.service;

import com.bank.exception.InsufficientFundsException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.enums.TransactionType;
import com.bank.modules.transaction.repository.TransactionRepository;
import com.bank.modules.transaction.request.NewTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setUUID("account-uuid");
        account.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void depositIncreasesBalance() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("25.50"))
                .transactionType("DEPOSIT")
                .description("top up")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(request, "account-uuid");

        assertEquals(0, new BigDecimal("125.50").compareTo(account.getBalance()));
        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawalDecreasesBalance() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("40.00"))
                .transactionType("WITHDRAWAL")
                .description("cash out")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(request, "account-uuid");

        assertEquals(0, new BigDecimal("60.00").compareTo(account.getBalance()));
        assertEquals(TransactionType.WITHDRAWAL, result.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
    }

    @Test
    void paymentDecreasesBalance() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("10.00"))
                .transactionType("PAYMENT")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request, "account-uuid");

        assertEquals(0, new BigDecimal("90.00").compareTo(account.getBalance()));
    }

    @Test
    void transferDecreasesBalance() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("15.00"))
                .transactionType("TRANSFER")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request, "account-uuid");

        assertEquals(0, new BigDecimal("85.00").compareTo(account.getBalance()));
    }

    @Test
    void insufficientFundsDoesNotPersist() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("150.00"))
                .transactionType("WITHDRAWAL")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        InsufficientFundsException ex = assertThrows(
                InsufficientFundsException.class,
                () -> transactionService.createTransaction(request, "account-uuid")
        );

        assertEquals("Insufficient funds for this transaction", ex.getMessage());
        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }
}
