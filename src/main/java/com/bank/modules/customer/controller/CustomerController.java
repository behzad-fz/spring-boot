package com.bank.modules.customer.controller;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.request.NewCustomerRequest;
import com.bank.modules.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> saveCustomer(@Valid @RequestBody NewCustomerRequest request) {

        var customer = customerService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
}
