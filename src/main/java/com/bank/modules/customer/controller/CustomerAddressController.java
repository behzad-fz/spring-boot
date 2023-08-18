package com.bank.modules.customer.controller;

import com.bank.modules.customer.entity.CustomerAddress;
import com.bank.modules.customer.request.NewCustomerAddressRequest;
import com.bank.modules.customer.service.CustomerAddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/customers/{customerUUID}/addresses")
public class CustomerAddressController {

    private final CustomerAddressService addressService;
    public CustomerAddressController(CustomerAddressService addressService) {
        this.addressService = addressService;
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
