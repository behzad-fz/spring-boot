package com.javaexample.java.modules.car.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "models")
@Data
@Builder
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer brand_id;
    private String name;
}
