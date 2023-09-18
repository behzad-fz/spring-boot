package com.bank.modules.customer.controller.auth;

import com.bank.controller.auth.AuthenticationRequest;
import com.bank.controller.auth.AuthenticationResponse;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.request.ChangeUserNamePasswordRequest;
import com.bank.modules.customer.service.CustomerAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/customer-auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthenticationService authenticationService;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder encoder;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/update-my-credentials")
    public ResponseEntity<Customer> changeCredentials(@RequestBody ChangeUserNamePasswordRequest request) {
        Customer customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customer.setUsername(request.getUsername());
        customer.setPassword(this.encoder.encode(request.getPassword()));
        customerRepository.save(customer);

        return ResponseEntity.ok(customer);
    }
}