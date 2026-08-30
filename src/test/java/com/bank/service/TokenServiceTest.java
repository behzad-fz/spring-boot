package com.bank.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {

    private static final String TEST_KEY = "QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=";

    private final TokenService tokenService = new TokenService(TEST_KEY);

    private UserDetails details(String username) {
        return User.withUsername(username).password("x").authorities("USER").build();
    }

    @Test
    void generatedTokenRoundTripsWithExternalizedKey() {
        String token = tokenService.generateToken(details("alice"), "customer");

        assertEquals("alice", tokenService.extractUserName(token));
        assertEquals("customer", tokenService.extractUserType(token));
        assertTrue(tokenService.isTokenValid(token, details("alice")));
    }

    @Test
    void tokenIsInvalidForAnotherSubject() {
        String token = tokenService.generateToken(details("alice"), "customer");

        assertTrue(!tokenService.isTokenValid(token, details("bob")));
    }

    @Test
    void blankKeyIsRejectedAtConstruction() {
        assertThrows(IllegalStateException.class, () -> new TokenService("   "));
    }
}