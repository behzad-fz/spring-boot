package com.bank.modules.transaction.repository;

import com.bank.modules.transaction.entity.ScheduledTransaction;
import com.bank.modules.transaction.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {
    List<ScheduledTransaction> findByStatusAndRunAtLessThanEqual(TransactionStatus status, LocalDateTime now);
}
