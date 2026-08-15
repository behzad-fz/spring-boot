package com.bank.modules.account.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MyAccountControllerTest {

    private AccountRepository accountRepository;
    private MyAccountController controller;

    private Customer authenticatedCustomer;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        controller = new MyAccountController(accountRepository);

        authenticatedCustomer = new Customer();
        authenticatedCustomer.setUUID("customer-uuid");
        authenticatedCustomer.setRole(CustomerRole.ORDINARY_CUSTOMER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedCustomer, null, authenticatedCustomer.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAccountReturnsOwnedAccount() {
        Customer owner = new Customer();
        owner.setUUID("customer-uuid");

        Account account = new Account();
        account.setUUID("account-uuid");
        account.setCustomer(owner);

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        Account result = controller.getAccount("account-uuid").getBody();

        assertEquals("account-uuid", result.getUUID());
    }

    @Test
    void getAccountReturns404WhenMissing() {
        when(accountRepository.findByUUID("unknown-account")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> controller.getAccount("unknown-account"));
    }

    @Test
    void getAccountReturns404WhenNotOwned() {
        Customer otherOwner = new Customer();
        otherOwner.setUUID("other-customer-uuid");

        Account account = new Account();
        account.setUUID("account-uuid");
        account.setCustomer(otherOwner);

        when(accountRepository.findByUUID("account-uuid")).thenReturn(account);

        assertThrows(ResourceNotFoundException.class, () -> controller.getAccount("account-uuid"));
    }
}
