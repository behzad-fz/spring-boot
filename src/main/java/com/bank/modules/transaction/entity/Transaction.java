package com.bank.modules.transaction.entity;

import com.bank.modules.account.entity.Account;
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
@Table(name = "transactions")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonIgnore
    private Long id;

    @Column(
            name = "transaction_number",
            columnDefinition = "VARCHAR(36)",
            nullable = false,
            unique = true
    )
    private String transactionNumber;

    @Column(
            columnDefinition = "decimal(18,2) DEFAULT 0",
            name = "amount",
            precision = 18,
            scale = 2,
            nullable = false
    )
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transaction_type",
            nullable = false,
            columnDefinition = "enum('DEPOSIT', 'WITHDRAWAL', 'PAYMENT', 'TRANSFER', 'CURRENCY_CONVERSION')"
    )
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "enum('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'DECLINED', 'CANCELED')"
    )
    private TransactionStatus status;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status_explanation")
    private String statusExplanation;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;

    @PrePersist
    private void onCreate() {
        initiatedAt = LocalDateTime.now();
        transactionNumber = java.util.UUID.randomUUID().toString();
        status = TransactionStatus.PENDING;
    }
}
