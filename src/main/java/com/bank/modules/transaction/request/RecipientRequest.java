package com.bank.modules.transaction.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientRequest {

    @NotNull
    private String fullName;

    private String email;

    @NotNull
    private String iban;

    @NotNull
    private String bankName;
}
