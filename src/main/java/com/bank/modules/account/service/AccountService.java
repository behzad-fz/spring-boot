package com.bank.modules.account.service;

import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.account.request.NewAccountRequest;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account save(NewAccountRequest request, String customerUUID) {
        var account = Account.builder()
                .type(request.getType())
                .balance(BigDecimal.valueOf(0))
                .status(AccountStatus.INACTIVE)
                .build();

        Customer customer = customerRepository.findByUUID(customerUUID);
        account.setCustomer(customer);

        return accountRepository.save(account);
    }
}
