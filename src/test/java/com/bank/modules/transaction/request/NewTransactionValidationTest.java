package com.bank.modules.transaction.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewTransactionValidationTest {

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
    void negativeAmountIsRejected() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("-100.00"))
                .transactionType("WITHDRAWAL")
                .build();

        Set<ConstraintViolation<NewTransaction>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void zeroAmountIsRejected() {
        NewTransaction request = NewTransaction.builder()
                .amount(BigDecimal.ZERO)
                .transactionType("DEPOSIT")
                .build();

        Set<ConstraintViolation<NewTransaction>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void positiveAmountIsAccepted() {
        NewTransaction request = NewTransaction.builder()
                .amount(new BigDecimal("25.50"))
                .transactionType("DEPOSIT")
                .build();

        Set<ConstraintViolation<NewTransaction>> violations = validator.validate(request);

        assertEquals(0, violations.size());
    }
}
