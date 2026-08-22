package com.bank.modules.transaction.service;

import com.bank.exception.InsufficientFundsException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountType;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerRole;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.transaction.request.NewTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void concurrentWithdrawalsCannotOverdrawAccount() throws Exception {
        Customer customer = customerRepository.save(Customer.builder()
                .firstName("Conc").lastName("Test")
                .email("conc-test@example.com").phoneNumber("+31000000000")
                .username("conc-test@example.com").password("x")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .role(CustomerRole.ORDINARY_CUSTOMER)
                .build());

        // NOTE: Account @PrePersist forces balance to 0 on insert, so set the
        // real starting balance via an update afterwards.
        Account account = accountRepository.save(Account.builder()
                .UUID("concurrency-test-account")
                .type(AccountType.CHECKING)
                .build());
        account.setCustomer(customer);
        account.setBalance(new BigDecimal("100.00"));
        accountRepository.save(account);

        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Object>> futures = new ArrayList<>();
        var startLatch = new java.util.concurrent.CountDownLatch(1);

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit((Callable<Object>) () -> {
                startLatch.await();
                try {
                    transactionService.createTransaction(NewTransaction.builder()
                            .amount(new BigDecimal("80.00"))
                            .transactionType("WITHDRAWAL")
                            .build(), "concurrency-test-account");
                    return Boolean.TRUE; // succeeded
                } catch (InsufficientFundsException e) {
                    return Boolean.FALSE; // correctly rejected
                }
            }));
        }

        startLatch.countDown();
        int successes = 0;
        int rejections = 0;
        for (Future<Object> f : futures) {
            if (Boolean.TRUE.equals(f.get())) successes++;
            else rejections++;
        }
        pool.shutdown();

        Account fresh = accountRepository.findByUUID(account.getUUID());

        assertEquals(1, successes,
                "exactly one withdrawal may succeed when balance covers only one");
        assertEquals(1, rejections,
                "the other withdrawal must be rejected, not silently overdraw");
        assertEquals(0, new BigDecimal("20.00").compareTo(fresh.getBalance()),
                "final balance must equal 100 - 80, never negative");
        assertTrue(fresh.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                "balance must never go negative regardless of interleaving");
    }
}
