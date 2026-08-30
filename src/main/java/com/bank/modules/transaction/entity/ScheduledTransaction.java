package com.bank.modules.transaction.entity;

import com.bank.modules.account.entity.Account;
import com.bank.modules.transaction.enums.Recurrence;
import com.bank.modules.transaction.enums.TransactionStatus;
import com.bank.modules.transaction.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transactions")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonIgnore
    private Long id;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "run_at", nullable = false)
    private LocalDateTime runAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "status_explanation")
    private String statusExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence")
    private Recurrence recurrence;

    @Column(name = "recurrence_end")
    private LocalDateTime recurrenceEnd;

    @Column(name = "occurrences_left")
    private Integer occurrencesLeft;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;
}
