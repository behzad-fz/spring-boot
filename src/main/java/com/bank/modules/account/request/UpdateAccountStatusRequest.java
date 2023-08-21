package com.bank.modules.account.request;

import com.bank.modules.account.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountStatusRequest {
    private AccountStatus status;
}
