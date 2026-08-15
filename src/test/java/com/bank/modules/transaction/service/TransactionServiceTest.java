package com.bank.modules.transaction.service;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.RecipientNotFoundException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerRole;
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
import com.bank.enums.Currency;
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
import java.time.LocalDateTime;
import java.util.List;

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

    @Mock
    private ScheduledTransactionRepository scheduledTransactionRepository;

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

    @Test
    void createTransactionOnUnknownAccountReturns404() {
        when(accountRepository.findByUUID("unknown-account")).thenReturn(null);

        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("25.50"))
                .transactionType("DEPOSIT")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction(request, "unknown-account"));

        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void payToRecipientOnUnknownAccountReturns404() {
        when(accountRepository.findByUUID("unknown-account")).thenReturn(null);

        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL91ABNA0417164300")
                .amount(new BigDecimal("30.00"))
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.payToRecipient("unknown-account", request));

        verify(transactionRepository, never()).save(any());
        verify(recipientRepository, never()).findByIban(anyString());
    }

    @Test
    void currencyConversionDebitsSourceAndCreditsTarget() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("100.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(customer);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.USD);
        target.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("20.00"))
                .exchangeRate(new BigDecimal("1.10"))
                .build();

        CurrencyConversionResult result = transactionService.convertCurrency("source-account", request);

        assertEquals(0, new BigDecimal("80.00").compareTo(source.getBalance()), "source debited by amount");
        assertEquals(0, new BigDecimal("72.00").compareTo(target.getBalance()), "target credited by amount * rate");
        assertEquals(TransactionType.CURRENCY_CONVERSION, result.sourceLeg().getTransactionType());
        assertEquals(TransactionType.CURRENCY_CONVERSION, result.targetLeg().getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, result.sourceLeg().getStatus());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void currencyConversionRejectsInsufficientFunds() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("10.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(customer);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.USD);
        target.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);

        CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("20.00"))
                .exchangeRate(new BigDecimal("1.10"))
                .build();

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.convertCurrency("source-account", request));

        assertEquals(0, new BigDecimal("10.00").compareTo(source.getBalance()));
        assertEquals(0, new BigDecimal("50.00").compareTo(target.getBalance()));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void currencyConversionRejectsSameAccount() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        Account account = new Account();
        account.setUUID("source-account");
        account.setBalance(new BigDecimal("100.00"));
        account.setCurrency(Currency.EUR);
        account.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("source-account")).thenReturn(account);

        CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                .targetAccountUUID("source-account")
                .amount(new BigDecimal("20.00"))
                .exchangeRate(new BigDecimal("1.10"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.convertCurrency("source-account", request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void currencyConversionRejectsCrossCustomerTarget() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");

        Customer other = new Customer();
        other.setUUID("other-customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("100.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(owner);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.USD);
        target.setCustomer(other);

        authenticateAs(owner);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);

        CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("20.00"))
                .exchangeRate(new BigDecimal("1.10"))
                .build();

        assertThrows(AccessDeniedException.class,
                () -> transactionService.convertCurrency("source-account", request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferDebitsSourceAndCreditsDestination() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("100.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(customer);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.EUR);
        target.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferRequest request = TransferRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("30.00"))
                .build();

        TransferResult result = transactionService.transferBetweenAccounts("source-account", request);

        assertEquals(0, new BigDecimal("70.00").compareTo(source.getBalance()), "source debited");
        assertEquals(0, new BigDecimal("80.00").compareTo(target.getBalance()), "destination credited");
        assertEquals(TransactionType.TRANSFER, result.sourceLeg().getTransactionType());
        assertEquals(TransactionType.TRANSFER, result.targetLeg().getTransactionType());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferRejectsInsufficientFunds() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("10.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(customer);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.EUR);
        target.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);

        TransferRequest request = TransferRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("30.00"))
                .build();

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.transferBetweenAccounts("source-account", request));

        assertEquals(0, new BigDecimal("10.00").compareTo(source.getBalance()));
        assertEquals(0, new BigDecimal("50.00").compareTo(target.getBalance()));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferRejectsCurrencyMismatch() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("100.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(customer);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.USD);
        target.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);

        TransferRequest request = TransferRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("30.00"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferBetweenAccounts("source-account", request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferRejectsCrossCustomerTarget() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");

        Customer other = new Customer();
        other.setUUID("other-customer-uuid");

        Account source = new Account();
        source.setUUID("source-account");
        source.setBalance(new BigDecimal("100.00"));
        source.setCurrency(Currency.EUR);
        source.setCustomer(owner);

        Account target = new Account();
        target.setUUID("target-account");
        target.setBalance(new BigDecimal("50.00"));
        target.setCurrency(Currency.EUR);
        target.setCustomer(other);

        authenticateAs(owner);

        when(accountRepository.findByUUID("source-account")).thenReturn(source);
        when(accountRepository.findByUUID("target-account")).thenReturn(target);

        TransferRequest request = TransferRequest.builder()
                .targetAccountUUID("target-account")
                .amount(new BigDecimal("30.00"))
                .build();

        assertThrows(AccessDeniedException.class,
                () -> transactionService.transferBetweenAccounts("source-account", request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsReturnsOrderedHistoryForOwnedAccount() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");
        account.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.findByAccountUUIDOrderByInitiatedAtDesc("account-uuid"))
                .thenReturn(List.of(new Transaction(), new Transaction()));

        List<Transaction> result = transactionService.getTransactions("account-uuid");

        assertEquals(2, result.size());
        verify(transactionRepository).findByAccountUUIDOrderByInitiatedAtDesc("account-uuid");
    }

    @Test
    void getTransactionsForUnknownAccountReturns404() {
        when(accountRepository.findByUUID("unknown-account")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactions("unknown-account"));

        verify(transactionRepository, never()).findByAccountUUIDOrderByInitiatedAtDesc(anyString());
    }

    @Test
    void getTransactionsForAnotherCustomersAccountIsRejected() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");
        account.setCustomer(owner);

        Customer other = new Customer();
        other.setUUID("other-customer-uuid");
        authenticateAs(other);

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        assertThrows(AccessDeniedException.class,
                () -> transactionService.getTransactions("account-uuid"));

        verify(transactionRepository, never()).findByAccountUUIDOrderByInitiatedAtDesc(anyString());
    }

    @Test
    void scheduleTransactionCreatesPendingScheduledTransaction() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");
        account.setCustomer(customer);

        authenticateAs(customer);

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(scheduledTransactionRepository.save(any(ScheduledTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleTransactionRequest request = ScheduleTransactionRequest.builder()
                .amount(new BigDecimal("25.00"))
                .description("rent")
                .runAt(LocalDateTime.now().plusDays(1))
                .build();

        ScheduledTransaction result = transactionService.scheduleTransaction("account-uuid", request);

        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertEquals(TransactionType.WITHDRAWAL, result.getTransactionType());
        verify(scheduledTransactionRepository).save(any(ScheduledTransaction.class));
    }

    @Test
    void scheduleTransactionForAnotherCustomersAccountIsRejected() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");
        account.setCustomer(owner);

        Customer other = new Customer();
        other.setUUID("other-customer-uuid");
        authenticateAs(other);

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        ScheduleTransactionRequest request = ScheduleTransactionRequest.builder()
                .amount(new BigDecimal("25.00"))
                .runAt(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(AccessDeniedException.class,
                () -> transactionService.scheduleTransaction("account-uuid", request));

        verify(scheduledTransactionRepository, never()).save(any());
    }

    @Test
    void processDueScheduledTransactionCompletesWhenFundsAvailable() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");
        account.setCustomer(customer);

        ScheduledTransaction scheduled = ScheduledTransaction.builder()
                .id(1L)
                .amount(new BigDecimal("25.00"))
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .account(account)
                .build();

        when(scheduledTransactionRepository.findByStatusAndRunAtLessThanEqual(any(), any()))
                .thenReturn(List.of(scheduled));
        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(scheduledTransactionRepository.save(any(ScheduledTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.processDueScheduledTransactions();

        assertEquals(TransactionStatus.COMPLETED, scheduled.getStatus());
        assertNotNull(scheduled.getProcessedAt());
        assertEquals(0, new BigDecimal("75.00").compareTo(account.getBalance()));
    }

    @Test
    void processDueScheduledTransactionFailsOnInsufficientFunds() {
        Customer customer = new Customer();
        customer.setUUID("customer-uuid");
        account.setCustomer(customer);

        account.setBalance(new BigDecimal("10.00"));

        ScheduledTransaction scheduled = ScheduledTransaction.builder()
                .id(1L)
                .amount(new BigDecimal("25.00"))
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .account(account)
                .build();

        when(scheduledTransactionRepository.findByStatusAndRunAtLessThanEqual(any(), any()))
                .thenReturn(List.of(scheduled));
        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);
        when(scheduledTransactionRepository.save(any(ScheduledTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.processDueScheduledTransactions();

        assertEquals(TransactionStatus.FAILED, scheduled.getStatus());
        assertNotNull(scheduled.getStatusExplanation());
        assertEquals(0, new BigDecimal("10.00").compareTo(account.getBalance()));
    }

    private void authenticateAs(Customer customer) {
        if (customer.getRole() == null) {
            customer.setRole(CustomerRole.ORDINARY_CUSTOMER);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));
    }
}
