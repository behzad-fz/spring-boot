package com.bank.modules.transaction.service;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.repository.RecipientRepository;
import com.bank.modules.transaction.request.RecipientRequest;
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
        Customer customer = customerRepository.findByUUID(customerUUID);

        return customer.getRecipients();
    }

    public Recipient save(RecipientRequest newRecipient, String customerUUID) {
        Recipient recipient = Recipient.builder()
                .fullName(newRecipient.getFullName())
                .email(newRecipient.getEmail())
                .iban(newRecipient.getIban())
                .bankName(newRecipient.getBankName())
                .build();

        Customer customer = customerRepository.findByUUID(customerUUID);

        recipient.setCustomer(customer);

        return recipientRepository.save(recipient);
    }

    public Recipient update(RecipientRequest request, Long id) {
        Recipient recipient = recipientRepository.findById(id).orElseThrow();
        recipient.setFullName(request.getFullName());
        recipient.setIban(request.getIban());
        recipient.setEmail(request.getEmail());
        recipient.setBankName(request.getBankName());

        return recipientRepository.save(recipient);
    }

    public void delete(Long id) {
        recipientRepository.deleteById(id);
    }
}
