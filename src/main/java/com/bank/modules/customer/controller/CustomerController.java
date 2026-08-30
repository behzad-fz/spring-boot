package com.bank.modules.customer.controller;

import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerCreationResponse;
import com.bank.modules.customer.request.CustomerRequest;
import com.bank.modules.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers () {
        List<Customer> customers = customerService.getCustomers();

        return ResponseEntity.ok(customers);
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("search")
    public ResponseEntity<List<Customer>> search(
            @RequestParam(required = true) String queryString
    ) {
        List<Customer> customers = customerService.searchForCustomer(queryString);

        return ResponseEntity.ok(customers);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<CustomerCreationResponse> saveCustomer(@Valid @RequestBody CustomerRequest request) {
        var customer = customerService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("{customerUUID}")
    public ResponseEntity<Customer> updateCustomer(
            @Valid @RequestBody CustomerRequest request,
            @PathVariable String customerUUID
    ) {
        Customer customer = customerService.update(request, customerUUID);

        return ResponseEntity.ok(customer);
    }

    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("{customerUUID}")
    public ResponseEntity<Void> delete(@PathVariable String customerUUID) {
        customerService.delete(customerUUID);

        return ResponseEntity.noContent().build();
    }
}
