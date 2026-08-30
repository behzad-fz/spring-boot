package com.bank.modules.transaction.request;

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

class RecipientRequestValidationTest {

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
        RecipientRequest request = RecipientRequest.builder()
                .fullName("Jane Doe")
                .iban("NL99ABNA0417164300")
                .bankName("TestBank")
                .build();

        Set<ConstraintViolation<RecipientRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("iban")));
    }

    @Test
    void validIbanIsAccepted() {
        RecipientRequest request = RecipientRequest.builder()
                .fullName("Jane Doe")
                .iban("NL91ABNA0417164300")
                .bankName("TestBank")
                .build();

        Set<ConstraintViolation<RecipientRequest>> violations = validator.validate(request);

        assertEquals(0, violations.size());
    }
}