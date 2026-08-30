package com.bank.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unhandledExceptionReturnsGeneric500WithoutLeakingDetails() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/transactions");

        Exception ex = new RuntimeException("secret internal detail: table X broken");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals("An unexpected error occurred", body.message());
        assertFalse(body.message().contains("secret"), "response must not leak internal details");
    }

    @Test
    void accessDeniedReturns403() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new AccessDeniedException("forbidden"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals(403, body.status());
        assertEquals("Forbidden", body.error());
        assertEquals("Access denied", body.message());
    }
}
