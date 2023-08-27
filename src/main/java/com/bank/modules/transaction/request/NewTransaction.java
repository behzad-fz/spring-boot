package com.bank.modules.transaction.request;

import com.bank.modules.transaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewTransaction {
    private BigDecimal amount;
    private String description;
    private TransactionType transactionType;
}
