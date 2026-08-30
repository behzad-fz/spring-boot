package com.bank.modules.account.helpers;

import java.math.BigInteger;

public final class IbanGenerator {

    private static final String COUNTRY_CODE = "NL";
    private static final String BANK_CODE = "BK";

    private IbanGenerator() {
    }

    public static String generateFromAccountNumber(String accountNumber) {
        String bban = BANK_CODE + accountNumber;
        String rearranged = bban + COUNTRY_CODE + "00";

        StringBuilder expanded = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                expanded.append(c);
            } else {
                expanded.append(c - 'A' + 10);
            }
        }

        int remainder = new BigInteger(expanded.toString()).mod(BigInteger.valueOf(97)).intValue();
        int checkDigits = 98 - remainder;

        return COUNTRY_CODE + String.format("%02d", checkDigits) + bban;
    }
}