package com.bank.modules.transaction.service;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.RecipientNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerRole;
import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.enums.TransactionType;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.repository.TransactionRepository;
import com.bank.modules.transaction.request.NewTransaction;
import com.bank.modules.transaction.request.RecipientPaymentRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private RecipientRepository recipientRepository;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    @Test
    void paymentToOwnedRecipientDebitsBalance() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        account.setCustomer(customer);

        Recipient recipient = Recipient.builder()
                .id(1L)
                .iban("NL91ABNA0417164300")
                .fullName("Jane Doe")
                .customer(customer)
                .build();

        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL91ABNA0417164300")
                .amount(new BigDecimal("30.00"))
                .description("rent")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(recipientRepository.findByIban("NL91ABNA0417164300")).thenReturn(recipient);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.payToRecipient("account-uuid", request);

        assertEquals(0, new BigDecimal("70.00").compareTo(account.getBalance()));
        assertEquals(TransactionType.PAYMENT, result.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        verify(recipientRepository).findByIban("NL91ABNA0417164300");
    }

    @Test
    void paymentToRecipientOwnedByAnotherCustomerIsRejected() {
        Customer accountOwner = new Customer();
        accountOwner.setUUID("customer-uuid");

        Customer otherCustomer = new Customer();
        otherCustomer.setUUID("other-customer-uuid");

        account.setCustomer(accountOwner);

        Recipient recipient = Recipient.builder()
                .id(1L)
                .iban("NL91ABNA0417164300")
                .fullName("Jane Doe")
                .customer(otherCustomer)
                .build();

        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL91ABNA0417164300")
                .amount(new BigDecimal("30.00"))
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(recipientRepository.findByIban("NL91ABNA0417164300")).thenReturn(recipient);

        assertThrows(RecipientNotFoundException.class,
                () -> transactionService.payToRecipient("account-uuid", request));

        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void paymentToUnknownRecipientIsRejected() {
        account.setCustomer(new Customer());

        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL91ABNA0417164300")
                .amount(new BigDecimal("30.00"))
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(recipientRepository.findByIban("NL91ABNA0417164300")).thenReturn(null);

        assertThrows(RecipientNotFoundException.class,
                () -> transactionService.payToRecipient("account-uuid", request));

        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void authenticatedCustomerCanTransactOnOwnAccount() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");
        account.setCustomer(customer);
        authenticateAs(customer);

        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("25.50"))
                .transactionType("DEPOSIT")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(request, "account-uuid");

        assertEquals(0, new BigDecimal("125.50").compareTo(account.getBalance()));
        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
    }

    @Test
    void authenticatedCustomerCannotTransactOnAnotherCustomersAccount() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");
        account.setCustomer(owner);

        Customer other = new Customer();
        other.setUUID("other-customer-uuid");
        authenticateAs(other);

        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("25.50"))
                .transactionType("DEPOSIT")
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        assertThrows(AccessDeniedException.class,
                () -> transactionService.createTransaction(request, "account-uuid"));

        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void authenticatedCustomerCannotPayToAnotherCustomersAccount() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");
        account.setCustomer(owner);

        Customer other = new Customer();
        other.setUUID("other-customer-uuid");
        authenticateAs(other);

        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL91ABNA0417164300")
                .amount(new BigDecimal("30.00"))
                .build();

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        assertThrows(AccessDeniedException.class,
                () -> transactionService.payToRecipient("account-uuid", request));

        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    private void authenticateAs(Customer customer) {
        if (customer.getRole() == null) {
            customer.setRole(CustomerRole.ORDINARY_CUSTOMER);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));
    }
}
