package com.bank.modules.transaction.service;

import com.bank.enums.Currency;
import com.bank.exception.UnknownCurrencyPairException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateServiceTest {

    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Test
    void sameCurrencyReturnsUnity() {
        assertEquals(0, BigDecimal.ONE.compareTo(exchangeRateService.rate(Currency.EUR, Currency.EUR)));
    }

    @Test
    void knownPairReturnsServerSideRate() {
        assertEquals(0, new BigDecimal("1.10").compareTo(exchangeRateService.rate(Currency.EUR, Currency.USD)));
    }

    @Test
    void anotherKnownPairReturnsServerSideRate() {
        assertEquals(0, new BigDecimal("0.85").compareTo(exchangeRateService.rate(Currency.EUR, Currency.GBP)));
    }
}
