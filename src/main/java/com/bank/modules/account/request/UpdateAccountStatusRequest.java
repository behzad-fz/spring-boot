package com.bank.modules.account.request;

import com.bank.modules.account.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountStatusRequest {
    @NotNull(message = "status must not be null")
    private AccountStatus status;
}
