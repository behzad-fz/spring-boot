package com.bank.modules.account.listener;

import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.repository.AccountRepository;
import com.bank.modules.customer.entity.Customer;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountActivatedListenerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JavaMailSender mailSender;

    private AccountActivatedListener listener;

    private final List<LogRecord> records = new ArrayList<>();

    @BeforeEach
    void setUp() {
        listener = new AccountActivatedListener(accountRepository, mailSender);
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        java.util.logging.Logger.getLogger(AccountActivatedListener.class.getName()).addHandler(capture);
    }

    @Test
    void activationEmailFailureIsLoggedAndDoesNotPropagate() {
        Customer customer = Customer.builder()
                .id(42L)
                .email("cust@example.com")
                .build();

        Account account = Account.builder()
                .id(7L)
                .status(AccountStatus.INACTIVE)
                .customer(customer)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
        org.mockito.Mockito.doThrow(new MailSendException("smtp down")).when(mailSender).send(any(MimeMessage.class));

        listener.getCurrentStatus(account);
        account.setStatus(AccountStatus.ACTIVE);
        listener.onStatusUpdate(account);

        boolean loggedSevere = records.stream()
                .anyMatch(r -> r.getLevel() == Level.SEVERE && r.getMessage().contains("account id=7"));
        assertFalse(records.isEmpty(), "failure must be logged, not silently dropped");
        assertTrue(loggedSevere, "SEVERE record with account context expected");
    }
}
