package com.bank.modules.account.helpers;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IbanGeneratorTest {

    private static final Pattern IBAN_PATTERN = Pattern.compile("^NL[0-9]{2}BK[A-Z][A-Z][0-9]{11}$");

    @Test
    void generatedIbanHasValidStructureAndCheckDigits() {
        String accountNumber = "AB12345678901";

        String iban = IbanGenerator.generateFromAccountNumber(accountNumber);

        assertTrue(IBAN_PATTERN.matcher(iban).matches());
        assertTrue(isMod97Valid(iban), "generated IBAN must pass mod-97");
    }

    @Test
    void differentAccountNumbersProduceDifferentIbans() {
        String first = IbanGenerator.generateFromAccountNumber("AB12345678901");
        String second = IbanGenerator.generateFromAccountNumber("CD98765432109");

        assertTrue(IBAN_PATTERN.matcher(first).matches());
        assertTrue(IBAN_PATTERN.matcher(second).matches());
        assertTrue(!first.equals(second));
        assertTrue(isMod97Valid(second));
    }

    private boolean isMod97Valid(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder expanded = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                expanded.append(c);
            } else {
                expanded.append(c - 'A' + 10);
            }
        }
        return new BigInteger(expanded.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }
}