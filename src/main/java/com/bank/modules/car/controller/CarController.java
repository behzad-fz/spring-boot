package com.bank.modules.car.controller;

import com.bank.modules.car.service.CarService;
import com.bank.modules.car.entity.Car;
import com.bank.modules.car.request.NewCarRequest;
import com.bank.modules.car.request.UpdateCarRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(carService.getAll());
    }

    @PostMapping
    public ResponseEntity<Car> saveCar(@RequestBody NewCarRequest request) {
        var car = carService.save(request);

        return ResponseEntity.ok(car);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, UpdateCarRequest request) {
        carService.update(id, request);
        return ResponseEntity.noContent().build();
    }
}
