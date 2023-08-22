package com.bank.modules.account.listener;

import com.bank.modules.account.entity.Account;
import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.repository.AccountRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDate;

public class AccountActivatedListener {

    private final AccountRepository accountRepository;
    private final JavaMailSender mailSender;

    private AccountStatus currentStatus;

    public AccountActivatedListener(@Lazy AccountRepository accountRepository, JavaMailSender mailSender) {
        this.accountRepository = accountRepository;
        this.mailSender = mailSender;
    }

    @PrePersist
    public void getCurrentStatus(Account account) {
        currentStatus = account.getStatus();
    }

    @PostUpdate
    public void onStatusUpdate(Account account) {
        if (currentStatus != AccountStatus.ACTIVE && account.getStatus() == AccountStatus.ACTIVE) {
            account.setOpenDate(LocalDate.now());

            accountRepository.save(account);

            sendActivationEmail(account);
        }
    }

    private void sendActivationEmail(Account account) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        try {
            helper.setTo(account.getCustomer().getEmail());
            helper.setSubject("Account Activation");
            helper.setText("Your account has been activated.");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // TODO: Report to sentry
        }
    }
}
