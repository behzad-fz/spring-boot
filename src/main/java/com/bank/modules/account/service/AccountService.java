package com.bank.modules.account.service;

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
        Customer customer =  customerRepository.findByUUID(customerUUID);

        return accountRepository.findByCustomerId(customer.getId());
    }

    public Account save(NewAccountRequest request, String customerUUID) {
        var account = Account.builder()
                .type(request.getType())
                .build();

        Customer customer = customerRepository.findByUUID(customerUUID);
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    public Account updateStatus(UpdateAccountStatusRequest request, String accountUUID) {
        Account account = accountRepository.findByUUID(accountUUID);
        account.setStatus(request.getStatus());

        return accountRepository.save(account);
    }
}
