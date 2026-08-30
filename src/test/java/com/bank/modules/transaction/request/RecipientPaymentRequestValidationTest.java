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

class RecipientPaymentRequestValidationTest {

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
    void invalidIbanIsRejected() {
        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL99ABNA0417164300")
                .amount(new BigDecimal("10.00"))
                .build();

        Set<ConstraintViolation<RecipientPaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("recipientIban")));
    }

    @Test
    void validIbanIsAccepted() {
        RecipientPaymentRequest request = RecipientPaymentRequest.builder()
                .recipientIban("NL91ABNA0417164300")
                .amount(new BigDecimal("10.00"))
                .build();

        Set<ConstraintViolation<RecipientPaymentRequest>> violations = validator.validate(request);

        assertEquals(0, violations.size());
    }
}