package com.bank.modules.customer.service;

import com.bank.controller.auth.AuthenticationRequest;
import com.bank.controller.auth.AuthenticationResponse;
import com.bank.entity.TokenType;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.entity.CustomerToken;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.service.TokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class CustomerAuthenticationService {

    private final CustomerRepository customerRepository;

    private final CustomerTokenRepository tokenRepository;

    private final TokenService tokenService;

    private final PasswordEncoder encoder;

    private final AuthenticationManager manager;

    private final Logger log = Logger.getLogger(this.getClass().getName());

    public CustomerAuthenticationService(
            CustomerRepository customerRepository,
            CustomerTokenRepository tokenRepository,
            TokenService tokenService,
            PasswordEncoder encoder,
            @Qualifier("customCustomerAuthenticationManager") AuthenticationManager manager
    ) {
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.encoder = encoder;
        this.manager = manager;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var customer = customerRepository.findByUsername(request.getUsername()).orElseThrow();

        var jwtToken = tokenService.generateToken(customer, "customer");

        revokeAllTokens(customer);
        saveToken(customer, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private void saveToken(Customer customer, String jwtToken) {
        var token = CustomerToken.builder()
                .customer(customer)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeAllTokens(Customer customer) {
        var validTokens = tokenRepository.findAllValidTokenByCustomer(customer.getId());
        if (validTokens.isEmpty())
            return;

        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validTokens);
    }
}