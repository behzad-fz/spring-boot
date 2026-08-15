package com.bank.modules.customer.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerCreationResponse;
import com.bank.modules.customer.entity.CustomerRole;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.CustomerRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
//@RequiredArgsConstructor // this can be used to automatically do constructor injections
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder encoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.encoder = passwordEncoder;
    }

    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchForCustomer(String queryString) {
        return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(queryString, queryString, queryString);
    }

    public CustomerCreationResponse save(CustomerRequest customerRequest) {
        String temporaryPassword = generateTemporaryPassword();

        var customer = Customer.builder()
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .email(customerRequest.getEmail())
                .phoneNumber(customerRequest.getPhoneNumber())
                .dateOfBirth(customerRequest.getDateOfBirth())
                .username(customerRequest.getEmail())
                .password(this.encoder.encode(temporaryPassword))
                .role(CustomerRole.ORDINARY_CUSTOMER)
                .build();

        Customer persisted = customerRepository.save(customer);

        return CustomerCreationResponse.builder()
                .customer(persisted)
                .temporaryPassword(temporaryPassword)
                .build();
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[12];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Customer update(CustomerRequest customerRequest, String uuid) {
        Customer customer = requireCustomer(uuid);

        customer.setFirstName(customerRequest.getFirstName());
        customer.setLastName(customerRequest.getLastName());
        customer.setEmail(customerRequest.getEmail());
        customer.setPhoneNumber(customerRequest.getPhoneNumber());
        customer.setDateOfBirth(customerRequest.getDateOfBirth());

        return customerRepository.save(customer);
    }

    public void delete(String uuid) {
        Customer customer = requireCustomer(uuid);

        customerRepository.delete(customer);
    }

    private Customer requireCustomer(String uuid) {
        Customer customer = customerRepository.findByUUID(uuid);

        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return customer;
    }
}
