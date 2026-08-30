package com.bank.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IbanValidatorTest {

    @Test
    void validNlIbanPasses() {
        assertTrue(IbanValidator.isValid("NL91ABNA0417164300"));
    }

    @Test
    void validDeIbanPasses() {
        assertTrue(IbanValidator.isValid("DE89370400440532013000"));
    }

    @Test
    void spacesSeparatedIbanPasses() {
        assertTrue(IbanValidator.isValid("NL91 ABNA 0417 1643 00"));
    }

    @Test
    void mixedCaseIbanPasses() {
        assertTrue(IbanValidator.isValid("nl91abna0417164300"));
    }

    @Test
    void wrongCheckDigitsFail() {
        assertFalse(IbanValidator.isValid("NL92ABNA0417164300"));
    }

    @Test
    void tooShortIbanFails() {
        assertFalse(IbanValidator.isValid("NL91ABNA04"));
    }

    @Test
    void invalidCharactersFail() {
        assertFalse(IbanValidator.isValid("NL91-ABNA-0417-1643-00"));
    }

    @Test
    void nullFails() {
        assertFalse(IbanValidator.isValid(null));
    }

    @Test
    void emptyFails() {
        assertFalse(IbanValidator.isValid(""));
    }
}