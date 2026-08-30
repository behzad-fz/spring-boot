package com.bank.modules.transaction.request;

import com.bank.validation.ValidIban;
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
    @ValidIban
    private String iban;

    @NotNull
    private String bankName;
}
