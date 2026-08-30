package com.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRecipientException extends RuntimeException {
    public DuplicateRecipientException(String message) {
        super(message);
    }
}
