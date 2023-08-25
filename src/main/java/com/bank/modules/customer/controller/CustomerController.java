package com.bank.modules.customer.controller;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.request.CustomerRequest;
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

    @GetMapping("search")
    public ResponseEntity<List<Customer>> search(
            @RequestParam(required = true) String queryString
    ) {
        List<Customer> customers = customerService.searchForCustomer(queryString);

        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<Customer> saveCustomer(@Valid @RequestBody CustomerRequest request) {

        var customer = customerService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping("{customerUUID}")
    public ResponseEntity<Customer> updateCustomer(
            @Valid @RequestBody CustomerRequest request,
            @PathVariable String customerUUID
    ) {
        Customer customer;
        
        try {
            customer = customerService.update(request, customerUUID);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("{customerUUID}")
    public ResponseEntity<Void> delete(@PathVariable String customerUUID) {
        try {
            customerService.delete(customerUUID);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.noContent().build();
    }
}
