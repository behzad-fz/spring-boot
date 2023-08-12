package com.bank.modules.car.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public void setManufacturedAt(String manufacturedAt) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.manufacturedAt = Optional.ofNullable(format.parse(manufacturedAt));
    }
}
