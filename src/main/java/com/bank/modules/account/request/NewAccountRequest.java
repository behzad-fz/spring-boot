package com.bank.modules.account.request;

import com.bank.modules.account.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewAccountRequest {

    @NotBlank(message = "Type is required")
    private AccountType type;

}
