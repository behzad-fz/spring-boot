package com.bank.modules.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "customer_addresses")
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "street", columnDefinition = "VARCHAR(30)", length = 30, nullable = false)
    private String street;
    @Column(name = "city", columnDefinition = "VARCHAR(30)", length = 30, nullable = false)
    private String city;
    @Column(name = "state", columnDefinition = "VARCHAR(30)", length = 30, nullable = false)
    private String state;
    @Column(name = "postal_code", columnDefinition = "VARCHAR(10)", length = 10, nullable = false)
    private String postalCode;
    @Column(name = "extra_information", columnDefinition = "TEXT")
    private String extraInformation;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}