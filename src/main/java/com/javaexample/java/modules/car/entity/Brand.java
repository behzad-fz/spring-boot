package com.javaexample.java.modules.car.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "brands")
@Data
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
}
