package com.bank.modules.customer.service;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerAddress;
import com.bank.modules.customer.repository.CustomerAddressRepository;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.NewCustomerAddressRequest;
import org.springframework.stereotype.Service;

@Service
public class CustomerAddressService {

    private CustomerAddressRepository addressRepository;
    private CustomerRepository customerRepository;

    public CustomerAddress save(NewCustomerAddressRequest addressRequest, String customerUUID) {
        var address = CustomerAddress.builder()
                .street(addressRequest.getStreet())
                .state(addressRequest.getState())
                .city(addressRequest.getCity())
                .postalCode(addressRequest.getPostalCode())
                .extraInformation(addressRequest.getExtraInformation())
                .build();

        Customer customer = customerRepository.findByUUID(customerUUID);
        address.setCustomer(customer);

        return addressRepository.save(address);
    }
}
