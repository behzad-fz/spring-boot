package com.bank.modules.transaction.service;

import com.bank.exception.DuplicateRecipientException;
import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.request.RecipientRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final CustomerRepository customerRepository;

    public RecipientService(RecipientRepository recipientRepository, CustomerRepository customerRepository) {
        this.recipientRepository = recipientRepository;
        this.customerRepository = customerRepository;
    }

    public List<Recipient> getAll(String customerUUID) {
        Customer customer = requireCustomer(customerUUID);

        return customer.getRecipients();
    }

    public Recipient save(RecipientRequest newRecipient, String customerUUID) {
        Recipient recipient = Recipient.builder()
                .fullName(newRecipient.getFullName())
                .email(newRecipient.getEmail())
                .iban(newRecipient.getIban())
                .bankName(newRecipient.getBankName())
                .build();

        Customer customer = requireCustomer(customerUUID);

        recipient.setCustomer(customer);

        try {
            return recipientRepository.save(recipient);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateRecipientException(
                    "A recipient with IBAN " + newRecipient.getIban() + " already exists");
        }
    }

    public Recipient update(RecipientRequest request, Long id, String customerUUID) {
        requireCustomer(customerUUID);
        Recipient recipient = requireOwnedRecipient(id, customerUUID);

        recipient.setFullName(request.getFullName());
        recipient.setIban(request.getIban());
        recipient.setEmail(request.getEmail());
        recipient.setBankName(request.getBankName());

        return recipientRepository.save(recipient);
    }

    public void delete(Long id, String customerUUID) {
        requireCustomer(customerUUID);
        Recipient recipient = requireOwnedRecipient(id, customerUUID);

        recipientRepository.delete(recipient);
    }

    private Recipient requireOwnedRecipient(Long id, String customerUUID) {
        Recipient recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        if (recipient.getCustomer() == null
                || recipient.getCustomer().getUUID() == null
                || !recipient.getCustomer().getUUID().equals(customerUUID)) {
            throw new ResourceNotFoundException("Recipient not found");
        }

        return recipient;
    }

    private Customer requireCustomer(String customerUUID) {
        Customer customer = customerRepository.findByUUID(customerUUID);

        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return customer;
    }
}
