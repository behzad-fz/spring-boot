package com.bank.modules.customer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCustomerAddressRequest {
    @NotBlank(message = "Street is required")
    @Size(min = 3, max = 30)
    private String street;
    @NotBlank(message = "City is required")
    @Size(min = 3, max = 30)
    private String city;
    @NotBlank(message = "State is required")
    @Size(min = 3, max = 30)
    private String state;
    @NotBlank(message = "Postal code is required")
    @Size(min = 6, max = 10)
    private String postalCode;
    private String extraInformation;
}
