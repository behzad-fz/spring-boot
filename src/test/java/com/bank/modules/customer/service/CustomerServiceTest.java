package com.bank.modules.customer.service;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerCreationResponse;
import com.bank.modules.customer.entity.CustomerRole;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.CustomerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, passwordEncoder);
    }

    private CustomerRequest request() {
        return CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phoneNumber("+31-600000000")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void saveEncodesARandomTemporaryPasswordAndReturnsItOnce() {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerCreationResponse first = customerService.save(request());
        CustomerCreationResponse second = customerService.save(request());

        assertNotNull(first.getTemporaryPassword());
        assertNotNull(second.getTemporaryPassword());
        assertNotEquals(first.getTemporaryPassword(), second.getTemporaryPassword(), "temporary passwords must be random");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(2)).save(captor.capture());

        Customer saved = captor.getAllValues().get(0);
        assertNotEquals(saved.getPassword(), "test-random", "must not use the hardcoded password");
        assertTrue(saved.getPassword().startsWith("encoded:"), "password must be stored encoded");
        assertEquals("alice@example.com", saved.getUsername());
        assertEquals(CustomerRole.ORDINARY_CUSTOMER, saved.getRole());
    }

    @Test
    void temporaryPasswordHasEnoughEntropy() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerCreationResponse response = customerService.save(request());

        assertNotNull(response.getTemporaryPassword());
        assertTrue(response.getTemporaryPassword().length() >= 16, "temporary password should be strong");
    }
}
