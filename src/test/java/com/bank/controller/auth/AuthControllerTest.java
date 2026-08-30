package com.bank.controller.auth;

import com.bank.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private static AuthenticationRequest authRequest(String username, String password) {
        return AuthenticationRequest.builder().username(username).password(password).build();
    }

    @Test
    void badCredentialsReturn401() {
        AuthenticationService service = mock(AuthenticationService.class);
        when(service.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        AuthController controller = new AuthController(service);

        ResponseEntity<?> response = controller.authenticate(authRequest("user", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void unexpectedErrorIsNotSwallowedInto401() {
        AuthenticationService service = mock(AuthenticationService.class);
        when(service.authenticate(any())).thenThrow(new IllegalStateException("database unavailable"));
        AuthController controller = new AuthController(service);

        assertThrows(IllegalStateException.class,
                () -> controller.authenticate(authRequest("user", "pass")));
    }
}