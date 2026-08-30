package com.bank.validation;

import java.math.BigInteger;
import java.util.regex.Pattern;

public final class IbanValidator {

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 34;
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$");

    private IbanValidator() {
    }

    public static boolean isValid(String iban) {
        if (iban == null) {
            return false;
        }

        String cleaned = iban.replaceAll("\\s+", "").toUpperCase();
        if (cleaned.length() < MIN_LENGTH || cleaned.length() > MAX_LENGTH) {
            return false;
        }
        if (!IBAN_PATTERN.matcher(cleaned).matches()) {
            return false;
        }

        String rearranged = cleaned.substring(4) + cleaned.substring(0, 4);
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