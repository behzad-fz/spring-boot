package com.bank.modules.customer.service;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.NewCustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor // this can be used to automatically do constructor injections
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer save(NewCustomerRequest customerRequest) {
        var customer = Customer.builder()
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .email(customerRequest.getEmail())
                .phoneNumber(customerRequest.getPhoneNumber())
                .dateOfBirth(customerRequest.getDateOfBirth())
                .build();

        return customerRepository.save(customer);
    }
}
