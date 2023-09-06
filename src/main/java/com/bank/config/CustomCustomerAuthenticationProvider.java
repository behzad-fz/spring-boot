package com.bank.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomCustomerAuthenticationProvider implements AuthenticationProvider {
    private final CustomCustomerDetailsService customCustomerDetailsService;
    private final PasswordEncoder passwordEncoder;


    public CustomCustomerAuthenticationProvider(CustomCustomerDetailsService customCustomerDetailsService, PasswordEncoder passwordEncoder) {
        this.customCustomerDetailsService = customCustomerDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails customer = customCustomerDetailsService.loadUserByUsername(username);

        if (customer != null && passwordEncoder.matches(password, customer.getPassword())) {
            // Authentication successful
            return new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities());
        } else {
            // Authentication failed
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
