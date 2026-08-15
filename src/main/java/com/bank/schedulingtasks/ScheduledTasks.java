package com.bank.schedulingtasks;

import com.bank.modules.transaction.entity.ScheduledTransaction;
import com.bank.modules.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {

    private final TransactionService transactionService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    public ScheduledTasks(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 5000)
    public void processDueTransactions() {
        List<ScheduledTransaction> processed = transactionService.processDueScheduledTransactions();

        if (!processed.isEmpty()) {
            log.info("Processed {} due scheduled transactions", processed.size());
        }
    }
}
