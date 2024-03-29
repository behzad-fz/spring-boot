package com.bank.modules.car.service;

import com.bank.modules.car.entity.Car;
import com.bank.modules.car.repository.CarRepository;
import com.bank.modules.car.request.NewCarRequest;
import com.bank.modules.car.request.UpdateCarRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public Car save(NewCarRequest request) {

        var car = Car.builder()
                .name(request.getName())
                .modelId(request.getModelId())
                .manufacturedAt(request.getManufacturedAt())
                .build();

        return carRepository.save(car);
    }

    public List<Car> getAll() {
        return carRepository.findAll();
    }

    public void update(Integer id, UpdateCarRequest request) {
        var car = carRepository.findById(id).orElseThrow();
        if (request.getName().isPresent()) {
            car.setName(request.getName().get());
        }
        if (request.getManufacturedAt().isPresent()) {
            car.setManufacturedAt(request.getManufacturedAt().get());
        }
        if (request.getModelId().isPresent()) {
            car.setModelId(request.getModelId().get());
        }

        carRepository.save(car);
    }
}
