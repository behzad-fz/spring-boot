package com.bank.modules.account.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.account.request.UpdateAccountStatusRequest;
import com.bank.modules.customer.entity.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private com.bank.modules.customer.repository.CustomerRepository customerRepository;

    private AccountService accountService;

    @Test
    void updateStatusSucceedsWhenAccountBelongsToCustomer() {
        accountService = new AccountService(accountRepository, customerRepository);

        Customer owner = Customer.builder().id(1L).UUID("owner-uuid").build();
        Account account = Account.builder().id(1L).UUID("acc-uuid").customer(owner)
                .status(AccountStatus.ACTIVE).build();

        when(accountRepository.findByUUID("acc-uuid")).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.updateStatus(
                new UpdateAccountStatusRequest(AccountStatus.SUSPENDED), "owner-uuid", "acc-uuid");

        assertEquals(AccountStatus.SUSPENDED, result.getStatus());
    }

    @Test
    void updateStatusDeniedWhenAccountBelongsToAnotherCustomer() {
        accountService = new AccountService(accountRepository, customerRepository);

        Customer owner = Customer.builder().id(1L).UUID("owner-uuid").build();
        Account account = Account.builder().id(1L).UUID("acc-uuid").customer(owner)
                .status(AccountStatus.ACTIVE).build();

        when(accountRepository.findByUUID("acc-uuid")).thenReturn(account);

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateStatus(
                        new UpdateAccountStatusRequest(AccountStatus.SUSPENDED), "attacker-uuid", "acc-uuid"));
    }

    @Test
    void updateStatusDeniedWhenAccountMissing() {
        accountService = new AccountService(accountRepository, customerRepository);

        when(accountRepository.findByUUID("missing")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateStatus(
                        new UpdateAccountStatusRequest(AccountStatus.SUSPENDED), "cust", "missing"));
    }
}
