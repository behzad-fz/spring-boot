package com.bank.modules.transaction.entity;

public record TransferResult(Transaction sourceLeg, Transaction targetLeg) {
}
