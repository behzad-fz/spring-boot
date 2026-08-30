package com.bank.modules.customer.controller.auth;

import com.bank.controller.auth.AuthenticationRequest;
import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.modules.customer.service.CustomerAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerAuthControllerAuthenticationTest {

    private static AuthenticationRequest authRequest(String username, String password) {
        return AuthenticationRequest.builder().username(username).password(password).build();
    }

    private CustomerAuthController controllerWith(CustomerAuthenticationService service) {
        return new CustomerAuthController(service,
                mock(CustomerRepository.class),
                mock(CustomerTokenRepository.class),
                mock(PasswordEncoder.class));
    }

    @Test
    void badCredentialsReturn401() {
        CustomerAuthenticationService service = mock(CustomerAuthenticationService.class);
        when(service.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        ResponseEntity<?> response =
                controllerWith(service).authenticate(authRequest("customer", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void unexpectedErrorIsNotSwallowedInto401() {
        CustomerAuthenticationService service = mock(CustomerAuthenticationService.class);
        when(service.authenticate(any())).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class,
                () -> controllerWith(service).authenticate(authRequest("customer", "pass")));
    }
}