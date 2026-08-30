package com.bank.modules.transaction.controller;

import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountType;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerRole;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.transaction.entity.Transaction;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.enums.TransactionType;
import com.bank.modules.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionHistoryTest {

    private static final String ACCOUNT_UUID = "history-test-account";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Account account;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(Customer.builder()
                .firstName("Hist").lastName("Test")
                .email("hist-test@example.com").phoneNumber("+31000000001")
                .username("hist-test@example.com").password("x")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .role(CustomerRole.ORDINARY_CUSTOMER)
                .build());

        account = accountRepository.save(Account.builder()
                .UUID(ACCOUNT_UUID)
                .type(AccountType.CHECKING)
                .build());
        account.setCustomer(customer);
        account.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(account);

        saveTransaction(new BigDecimal("100.00"), TransactionType.DEPOSIT, LocalDateTime.of(2026, 1, 15, 10, 0));
        saveTransaction(new BigDecimal("40.00"), TransactionType.WITHDRAWAL, LocalDateTime.of(2026, 2, 15, 10, 0));
        saveTransaction(new BigDecimal("25.00"), TransactionType.TRANSFER, LocalDateTime.of(2026, 3, 15, 10, 0));
    }

    private MockHttpServletRequestBuilder asOwner(String uri, String accountUUID) {
        return get(uri, accountUUID).with(SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities())));
    }

    private void saveTransaction(BigDecimal amount, TransactionType type, LocalDateTime initiatedAt) {
        Transaction txn = transactionRepository.save(Transaction.builder()
                .amount(amount)
                .transactionType(type)
                .account(account)
                .build());
        txn.setInitiatedAt(initiatedAt);
        txn.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(txn);
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void paginationReturnsCorrectlyPagedContent() throws Exception {
        mockMvc.perform(asOwner("/api/v1/accounts/{uuid}/transactions", ACCOUNT_UUID)
                        .param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].transactionType").value("TRANSFER"));
    }

    @Test
    void typeFilterRestrictsResults() throws Exception {
        mockMvc.perform(asOwner("/api/v1/accounts/{uuid}/transactions", ACCOUNT_UUID)
                        .param("type", "WITHDRAWAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].transactionType").value("WITHDRAWAL"));
    }

    @Test
    void amountBoundsFilterResults() throws Exception {
        mockMvc.perform(asOwner("/api/v1/accounts/{uuid}/transactions", ACCOUNT_UUID)
                        .param("minAmount", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void dateRangeFilterResults() throws Exception {
        mockMvc.perform(asOwner("/api/v1/accounts/{uuid}/transactions", ACCOUNT_UUID)
                        .param("from", "2026-02-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void invalidTypeParamReturns400() throws Exception {
        mockMvc.perform(asOwner("/api/v1/accounts/{uuid}/transactions", ACCOUNT_UUID)
                        .param("type", "BOGUS"))
                .andExpect(status().isBadRequest());
    }
}