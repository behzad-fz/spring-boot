package com.bank.modules.transaction.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleTransactionRequest {

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;

    @NotNull
    @Future(message = "Run-at must be in the future")
    private LocalDateTime runAt;
}
