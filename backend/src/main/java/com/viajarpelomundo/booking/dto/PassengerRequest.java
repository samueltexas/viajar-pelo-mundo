package com.viajarpelomundo.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PassengerRequest(

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @NotNull
    @PastOrPresent
    LocalDate dateOfBirth

) {
}
