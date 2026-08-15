package com.bank.modules.transaction.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.request.RecipientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipientServiceTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private CustomerRepository customerRepository;

    private RecipientService recipientService;

    @BeforeEach
    void setUp() {
        recipientService = new RecipientService(recipientRepository, customerRepository);
    }

    @Test
    void getAllForUnknownCustomerReturns404() {
        when(customerRepository.findByUUID("unknown-customer")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> recipientService.getAll("unknown-customer"));
    }

    @Test
    void saveForUnknownCustomerReturns404() {
        when(customerRepository.findByUUID("unknown-customer")).thenReturn(null);

        RecipientRequest request = RecipientRequest.builder()
                .fullName("Jane Doe")
                .iban("NL91ABNA0417164300")
                .bankName("TestBank")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> recipientService.save(request, "unknown-customer"));

        verify(recipientRepository, never()).save(any());
    }
}
