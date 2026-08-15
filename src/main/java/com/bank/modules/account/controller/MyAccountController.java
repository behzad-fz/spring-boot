package com.bank.modules.account.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class MyAccountController {

    private final AccountRepository accountRepository;

    public MyAccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/customer-accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        Customer customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(customer.getAccounts());
    }

    @GetMapping("/customer-accounts/{accountUUID}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountUUID) {
        Customer customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Account account = accountRepository.findByUUID(accountUUID);

        if (account == null || account.getCustomer() == null
                || !account.getCustomer().getUUID().equals(customer.getUUID())) {
            throw new ResourceNotFoundException("Account not found");
        }

        return ResponseEntity.ok(account);
    }
}
