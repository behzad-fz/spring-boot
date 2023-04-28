package com.javaexample.java.modules.car.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCarRequest {

    private Optional<String> name;
    private Optional<Integer> modelId;
    private Optional<Date> manufacturedAt;
}
