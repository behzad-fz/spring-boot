package com.bank.modules.account.controller;

import com.bank.modules.account.entity.Account;
import com.bank.modules.customer.entity.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/v1")
public class MyAccountController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @GetMapping("/customer-accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        Customer customer = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(customer.getAccounts());
    }
}
