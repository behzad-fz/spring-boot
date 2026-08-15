package com.bank.modules.customer.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerAddress;
import com.bank.modules.customer.repository.CustomerAddressRepository;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.NewCustomerAddressRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerAddressService {

    private final CustomerAddressRepository addressRepository;
    private final CustomerRepository customerRepository;


    public CustomerAddressService(CustomerAddressRepository addressRepository, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    public List<CustomerAddress> getAll(String customerUUID) {
        Customer customer = requireCustomer(customerUUID);

        return addressRepository.findByCustomerId(customer.getId());
    }

    public CustomerAddress save(NewCustomerAddressRequest addressRequest, String customerUUID) {
        var address = CustomerAddress.builder()
                .street(addressRequest.getStreet())
                .state(addressRequest.getState())
                .city(addressRequest.getCity())
                .postalCode(addressRequest.getPostalCode())
                .extraInformation(addressRequest.getExtraInformation())
                .build();


        Customer customer = requireCustomer(customerUUID);
        address.setCustomer(customer);

        return addressRepository.save(address);
    }

    private Customer requireCustomer(String customerUUID) {
        Customer customer = customerRepository.findByUUID(customerUUID);

        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return customer;
    }
}
