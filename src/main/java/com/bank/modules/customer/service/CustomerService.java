package com.bank.modules.customer.service;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.CustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//@RequiredArgsConstructor // this can be used to automatically do constructor injections
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    public Customer save(CustomerRequest customerRequest) {
        var customer = Customer.builder()
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .email(customerRequest.getEmail())
                .phoneNumber(customerRequest.getPhoneNumber())
                .dateOfBirth(customerRequest.getDateOfBirth())
                .build();

        return customerRepository.save(customer);
    }

    public Customer update(CustomerRequest customerRequest, String uuid) throws Exception {
        Customer customer = customerRepository.findByUUID(uuid);

        if (customer == null) {
            throw new Exception("Customer not found");
        }

        customer.setFirstName(customerRequest.getFirstName());
        customer.setLastName(customerRequest.getLastName());
        customer.setEmail(customerRequest.getEmail());
        customer.setPhoneNumber(customerRequest.getPhoneNumber());
        customer.setDateOfBirth(customerRequest.getDateOfBirth());

        return customerRepository.save(customer);
    }

    public void delete(String uuid) throws Exception {
        Customer customer = customerRepository.findByUUID(uuid);

        if (customer == null) {
            throw new Exception("Custoemr not fount");
        }

        customerRepository.delete(customer);
    }
}
