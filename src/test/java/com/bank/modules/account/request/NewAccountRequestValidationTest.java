package com.bank.modules.account.request;

import com.bank.modules.account.enums.AccountType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewAccountRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void missingCurrencyIsRejected() {
        NewAccountRequest request = NewAccountRequest.builder()
                .type(AccountType.SAVINGS)
                .build();

        Set<ConstraintViolation<NewAccountRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void invalidCurrencyIsRejected() {
        NewAccountRequest request = NewAccountRequest.builder()
                .type(AccountType.SAVINGS)
                .currency("XYZ")
                .build();

        Set<ConstraintViolation<NewAccountRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void validCurrencyIsAccepted() {
        NewAccountRequest request = NewAccountRequest.builder()
                .type(AccountType.SAVINGS)
                .currency("EUR")
                .build();

        Set<ConstraintViolation<NewAccountRequest>> violations = validator.validate(request);

        assertEquals(0, violations.size());
    }
}
