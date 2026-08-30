package com.bank.modules.account.service;

import com.bank.enums.Currency;
import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.account.request.NewAccountRequest;
import com.bank.modules.account.request.UpdateAccountStatusRequest;
import com.bank.modules.customer.entity.Customer;
import com.bank.modules.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public List<Account> getAll(String customerUUID) {
        Customer customer = requireCustomer(customerUUID);

        return accountRepository.findByCustomerId(customer.getId());
    }

    public Account save(NewAccountRequest request, String customerUUID) {
        var account = Account.builder()
                .type(request.getType())
                .currency(Currency.valueOf(request.getCurrency()))
                .build();

        Customer customer = requireCustomer(customerUUID);
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    public Account updateStatus(UpdateAccountStatusRequest request, String customerUUID, String accountUUID) {
        Account account = accountRepository.findByUUID(accountUUID);

        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        if (account.getCustomer() == null || !account.getCustomer().getUUID().equals(customerUUID)) {
            throw new ResourceNotFoundException("Account not found");
        }

        account.setStatus(request.getStatus());

        return accountRepository.save(account);
    }

    private Customer requireCustomer(String customerUUID) {
        Customer customer = customerRepository.findByUUID(customerUUID);

        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return customer;
    }
}
