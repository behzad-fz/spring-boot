package com.bank.modules.transaction.service;

import com.bank.enums.Currency;
import com.bank.exception.UnknownCurrencyPairException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Resolves currency-conversion rates server-side so that conversions never trust a
 * client-supplied rate. The built-in table is a placeholder pending a real FX feed;
 * replace {@link #DEFAULT_RATES} with a provider-backed lookup without changing callers.
 */
@Service
public class ExchangeRateService {

    private static final Map<String, BigDecimal> DEFAULT_RATES = Map.of(
            "EUR_USD", new BigDecimal("1.10"),
            "USD_EUR", new BigDecimal("0.91"),
            "EUR_GBP", new BigDecimal("0.85"),
            "GBP_EUR", new BigDecimal("1.18"),
            "USD_GBP", new BigDecimal("0.78"),
            "GBP_USD", new BigDecimal("1.28")
    );

    public BigDecimal rate(Currency from, Currency to) {
        if (from == to) {
            return BigDecimal.ONE;
        }

        BigDecimal rate = DEFAULT_RATES.get(from.name() + "_" + to.name());

        if (rate == null) {
            throw new UnknownCurrencyPairException(
                    "No exchange rate configured for " + from + " -> " + to);
        }

        return rate;
    }
}
