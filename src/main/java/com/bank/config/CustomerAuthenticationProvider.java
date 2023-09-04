package com.bank.config;

import com.bank.modules.customer.service.CustomerDetailsService;
import com.bank.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class CustomerAuthenticationProvider implements AuthenticationProvider {
    private final CustomerDetailsService customUserDetailsService;

    public CustomerAuthenticationProvider(CustomerDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
