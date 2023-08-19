package com.bank.modules.customer.controller;

import com.bank.modules.customer.entity.CustomerAddress;
import com.bank.modules.customer.request.NewCustomerAddressRequest;
import com.bank.modules.customer.service.CustomerAddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers/{customerUUID}/addresses")
public class CustomerAddressController {

    private final CustomerAddressService addressService;
    public CustomerAddressController(CustomerAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerAddress>> getAddresses(@PathVariable String customerUUID) {
        List<CustomerAddress> addresses = addressService.getAll(customerUUID);

        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<CustomerAddress> saveAddress(
            @Valid @RequestBody NewCustomerAddressRequest addressRequest,
            @PathVariable String customerUUID
    ) {
        CustomerAddress address = addressService.save(addressRequest, customerUUID);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }
}
