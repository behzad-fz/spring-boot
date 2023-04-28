package com.javaexample.java.modules.car.repository;

import com.javaexample.java.modules.car.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {
}
