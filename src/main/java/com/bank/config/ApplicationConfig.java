package com.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomUserAuthenticationProvider customAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomUserAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    public CustomCustomerAuthenticationProvider customCustomerAuthenticationProvider(CustomCustomerDetailsService customerDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomCustomerAuthenticationProvider(customerDetailsService, passwordEncoder);
    }

    @Bean(name = "customAuthenticationManager")
    @Primary
    public AuthenticationManager customAuthenticationManager(CustomUserAuthenticationProvider customUserAuthenticationProvider) {
        return new ProviderManager(customUserAuthenticationProvider);
    }

    @Bean(name = "customCustomerAuthenticationManager")
    public AuthenticationManager customCustomerAuthenticationManager(CustomCustomerAuthenticationProvider customCustomerAuthProvider) {
        return new ProviderManager(customCustomerAuthProvider);
    }
}