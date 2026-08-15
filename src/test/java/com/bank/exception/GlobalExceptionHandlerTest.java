package com.bank.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationErrorsShareTheUnifiedShape() {
        BindException ex = new BindException(new Object(), "target");
        ex.addError(new FieldError("target", "amount", "Amount must be positive"));

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Validation failed", body.error());
        assertTrue(body.fieldErrors().containsKey("amount"));
    }

    @Test
    void insufficientFundsSharesTheUnifiedShape() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientFunds(new InsufficientFundsException("Insufficient funds for this transaction"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Bad Request", body.error());
        assertEquals("Insufficient funds for this transaction", body.message());
        assertNull(body.fieldErrors());
    }

    @Test
    void notFoundSharesTheUnifiedShape() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Account not found"));

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Not Found", body.error());
        assertEquals("Account not found", body.message());
    }

    @Test
    void illegalArgumentIsBadRequestNotServerError() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Invalid type of transaction"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Bad Request", body.error());
        assertEquals("Invalid type of transaction", body.message());
    }
}
