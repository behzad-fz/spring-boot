package com.bank.modules.transaction.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.request.RecipientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void updateOfRecipientOwnedByAnotherCustomerReturns404() {
        Customer owner = new Customer();
        owner.setUUID("owner-uuid");
        when(customerRepository.findByUUID("owner-uuid")).thenReturn(owner);

        Customer other = new Customer();
        other.setUUID("other-uuid");

        Recipient recipient = Recipient.builder()
                .id(1L)
                .iban("NL91ABNA0417164300")
                .customer(other)
                .build();
        when(recipientRepository.findById(1L)).thenReturn(Optional.of(recipient));

        RecipientRequest request = RecipientRequest.builder()
                .fullName("Jane Doe")
                .iban("NL91ABNA0417164300")
                .bankName("TestBank")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> recipientService.update(request, 1L, "owner-uuid"));

        verify(recipientRepository, never()).save(any());
    }

    @Test
    void updateOfOwnedRecipientSucceeds() {
        Customer owner = new Customer();
        owner.setUUID("owner-uuid");
        when(customerRepository.findByUUID("owner-uuid")).thenReturn(owner);

        Recipient recipient = Recipient.builder()
                .id(1L)
                .fullName("Old Name")
                .iban("NL91ABNA0417164300")
                .bankName("OldBank")
                .customer(owner)
                .build();
        when(recipientRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(recipientRepository.save(any(Recipient.class))).thenAnswer(inv -> inv.getArgument(0));

        RecipientRequest request = RecipientRequest.builder()
                .fullName("New Name")
                .iban("NL91ABNA0417164300")
                .bankName("NewBank")
                .build();

        Recipient result = recipientService.update(request, 1L, "owner-uuid");

        assertEquals("New Name", result.getFullName());
        assertEquals("NewBank", result.getBankName());
    }

    @Test
    void deleteOfRecipientOwnedByAnotherCustomerReturns404() {
        Customer owner = new Customer();
        owner.setUUID("owner-uuid");
        when(customerRepository.findByUUID("owner-uuid")).thenReturn(owner);

        Customer other = new Customer();
        other.setUUID("other-uuid");

        Recipient recipient = Recipient.builder()
                .id(1L)
                .iban("NL91ABNA0417164300")
                .customer(other)
                .build();
        when(recipientRepository.findById(1L)).thenReturn(Optional.of(recipient));

        assertThrows(ResourceNotFoundException.class,
                () -> recipientService.delete(1L, "owner-uuid"));

        verify(recipientRepository, never()).delete(any());
    }
}
