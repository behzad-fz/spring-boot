package com.bank.modules.account.request;

import com.bank.modules.account.enums.AccountType;
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
}
