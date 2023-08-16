package com.bank.modules.customer.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
        name = "uuid",
        columnDefinition = "VARCHAR(36)",
        nullable = false,
        unique = true
    )
    private String UUID;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", columnDefinition = "DATE", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @PrePersist
    private void generateUuid() {
        if (UUID == null) {
            UUID = java.util.UUID.randomUUID().toString();
        }
    }

//    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
//    private List<Address> addresses = new ArrayList<>();

}
