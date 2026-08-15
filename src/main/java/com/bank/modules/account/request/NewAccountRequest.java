package com.bank.modules.account.request;

import com.bank.modules.account.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewAccountRequest {
    private AccountType type;

    @NotNull
    @Pattern(regexp = "EUR|USD|GBP", message = "Invalid currency")
    private String currency;
}
