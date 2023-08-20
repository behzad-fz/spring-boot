package com.bank.modules.account.helpers;

import java.util.Random;

public class AccountNumberGenerator {
    private static final Random RANDOM = new Random();

    public static String generateAccountNumber() {
        String letters = generateRandomLetters(4);
        String numbers = generateRandomNumbers(11);
        return letters + numbers;
    }

    private static String generateRandomLetters(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (RANDOM.nextInt(26) + 'A');
            sb.append(randomChar);
        }
        return sb.toString();
    }

    private static String generateRandomNumbers(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
