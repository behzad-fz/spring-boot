package com.bank.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Map<String, String>>> handleValidationException(BindException ex) {

        Map<String, Map<String, String>> errorResponse = new HashMap<>();
        Map<String, String> validationErrors = new HashMap<>();

        ex
            .getFieldErrors()
            .forEach(
                    error -> validationErrors.put(error.getField(), error.getDefaultMessage())
            );

        errorResponse.put("Validation errors", validationErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
