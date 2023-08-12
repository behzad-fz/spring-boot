package com.bank.modules.car.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCarRequest {

    private String name;
    private Integer modelId;
    private Date manufacturedAt;
}
