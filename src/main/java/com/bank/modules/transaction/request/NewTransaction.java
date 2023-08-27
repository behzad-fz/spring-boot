package com.bank.modules.transaction.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotNull
    private BigDecimal amount;

    @Pattern(regexp = "DEPOSIT|WITHDRAWAL|PAYMENT|TRANSFER|CURRENCY_CONVERSION", message = "Invalid type of transaction")
    private String transactionType;

    private String description;
}
