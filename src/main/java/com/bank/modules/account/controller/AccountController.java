package com.bank.modules.account.controller;

import com.bank.modules.account.entity.Account;
import com.bank.modules.account.request.NewAccountRequest;
import com.bank.modules.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/customers/{customerUUID}/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    private ResponseEntity<Account> save(
            @Valid @RequestBody NewAccountRequest request,
            @PathVariable String customerUUID
    ) {
        Account account = accountService.save(request, customerUUID);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
}
