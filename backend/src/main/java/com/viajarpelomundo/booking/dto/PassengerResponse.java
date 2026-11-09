package com.viajarpelomundo.booking.dto;

import com.viajarpelomundo.booking.entity.BookingPassenger;
import java.time.LocalDate;

public record PassengerResponse(
    Long id,
    String firstName,
    String lastName,
    LocalDate dateOfBirth
) {
    public static PassengerResponse from(
        BookingPassenger passenger
    ) {
        return new PassengerResponse(
            passenger.getId(),
            passenger.getFirstName(),
            passenger.getLastName(),
            passenger.getDateOfBirth()
        );
    }
}
