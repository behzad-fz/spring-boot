package com.bank.modules.customer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 50)
    private String lastName;

//    @NotBlank(message = "DOB is required")
    @Past(message = "DOB must be in the past!")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is required")
    @Size(min = 3, max = 50)
    private String email;

    @NotBlank(message = "Phone Number is required")
    @Size(min = 3, max = 20)
    private String phoneNumber;
}
