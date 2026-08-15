package com.bank.modules.transaction.entity;

public record CurrencyConversionResult(Transaction sourceLeg, Transaction targetLeg) {
}
