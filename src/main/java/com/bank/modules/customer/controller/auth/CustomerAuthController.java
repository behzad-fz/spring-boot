package com.bank.modules.customer.controller.auth;

import com.bank.controller.auth.AuthenticationRequest;
import com.bank.controller.auth.AuthenticationResponse;
import com.bank.exception.ErrorResponse;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerToken;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.modules.customer.request.ChangeUserNamePasswordRequest;
import com.bank.modules.customer.service.CustomerAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customer-auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthenticationService authenticationService;
    private final CustomerRepository customerRepository;
    private final CustomerTokenRepository customerTokenRepository;
    private final PasswordEncoder encoder;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (AuthenticationException ex) {
            ErrorResponse body = ErrorResponse.of(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Invalid credentials"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }

    @PutMapping("/update-my-credentials")
    public ResponseEntity<Customer> changeCredentials(@Valid @RequestBody ChangeUserNamePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Customer customer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        customer.setUsername(request.getUsername());
        customer.setPassword(this.encoder.encode(request.getPassword()));

        Customer saved = customerRepository.save(customer);

        List<CustomerToken> validTokens = customerTokenRepository.findAllValidTokenByCustomer(customer.getId());
        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        customerTokenRepository.saveAll(validTokens);

        return ResponseEntity.ok(saved);
    }
}