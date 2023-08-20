package com.bank.modules.customer.controller;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.request.NewCustomerRequest;
import com.bank.modules.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers () {
        List<Customer> customers = customerService.getCustomers();

        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<Customer> saveCustomer(@Valid @RequestBody NewCustomerRequest request) {

        var customer = customerService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
}
