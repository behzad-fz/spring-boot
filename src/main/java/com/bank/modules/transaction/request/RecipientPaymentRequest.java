package com.bank.modules.transaction.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientPaymentRequest {

    @NotBlank
    private String recipientIban;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
