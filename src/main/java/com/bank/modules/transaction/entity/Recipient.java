package com.bank.modules.transaction.entity;

import com.bank.modules.account.entity.Account;
import com.bank.modules.customer.entity.Customer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "recipients",
    uniqueConstraints = @UniqueConstraint(columnNames = {"iban"})
)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
        name = "full_name",
        columnDefinition = "VARCHAR(100)",
        nullable = false
    )
    private String fullName;

    @Column(
            name = "email",
            columnDefinition = "VARCHAR(100)",
            nullable = true
    )
    private String email;

    @Column(
            name = "iban",
            columnDefinition = "VARCHAR(30)",
            nullable = false,
            unique = true
    )
    private String iban;

    @Column(
            name = "bank_name",
            columnDefinition = "VARCHAR(50)",
            nullable = false
    )
    private String bankName;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;
}
