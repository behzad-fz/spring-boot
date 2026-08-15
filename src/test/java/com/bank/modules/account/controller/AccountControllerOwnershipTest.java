package com.bank.modules.account.controller;

import com.bank.modules.account.service.AccountService;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AccountControllerOwnershipTest {

    private AccountController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountController(mock(AccountService.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerCanAccessOwnAccounts() {
        authenticate("customer-uuid");

        assertDoesNotThrow(() -> requireCustomerOwns("customer-uuid"));
    }

    @Test
    void customerCannotAccessAnotherCustomersAccounts() {
        authenticate("customer-uuid");

        assertThrows(AccessDeniedException.class, () -> requireCustomerOwns("other-customer-uuid"));
    }

    @Test
    void staffUserIsNotRestricted() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new Object(), null));

        assertDoesNotThrow(() -> requireCustomerOwns("any-customer-uuid"));
    }

    private void authenticate(String customerUUID) {
        Customer customer = new Customer();
        customer.setUUID(customerUUID);
        customer.setRole(CustomerRole.ORDINARY_CUSTOMER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));
    }

    private void requireCustomerOwns(String customerUUID) throws Exception {
        Method method = AccountController.class.getDeclaredMethod("requireCustomerOwns", String.class);
        method.setAccessible(true);
        try {
            method.invoke(controller, customerUUID);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }
}
