package com.bank.modules.account.entity;

import com.bank.modules.account.enums.AccountStatus;
import com.bank.modules.account.enums.AccountType;
import com.bank.modules.account.helpers.AccountNumberGenerator;
import com.bank.modules.customer.entity.Customer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonIgnore
    private Long id;

    @Column(
        name = "uuid",
        columnDefinition = "VARCHAR(36)",
        nullable = false,
        unique = true
    )
    private String UUID;

    @Column(name = "account_number", columnDefinition = "VARCHAR(15)", length = 15, unique = true, nullable = false)
    private String accountNumber;

    @Column(
        columnDefinition = "decimal(18,2) DEFAULT 0",
        name = "balance",
        precision = 18,
        scale = 2,
        nullable = false
    )
    private BigDecimal balance;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "last_transaction")
    private LocalDate lastTransaction;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "enum('ACTIVE', 'INACTIVE', 'CLOSED', 'SUSPENDED', 'UNDER_INVESTIGATION')"
    )
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "type",
        nullable = false,
        columnDefinition = "enum('SAVINGS', 'CHECKING', 'CREDIT', 'LOAN', 'MORTGAGE')"
    )
    private AccountType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        accountNumber = AccountNumberGenerator.generateAccountNumber();
        generateUuid();
        balance = BigDecimal.valueOf(0);
        status = AccountStatus.INACTIVE;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void generateUuid() {
        if (UUID == null) {
            UUID = java.util.UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
