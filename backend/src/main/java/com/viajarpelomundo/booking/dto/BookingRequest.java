package com.viajarpelomundo.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookingRequest(

    @NotNull(message = "Outbound flight is required")
    Long flightId,

    Long returnFlightId,

    @NotNull(message = "Passenger count is required")
    @Min(value = 1, message = "At least one passenger is required")
    @Max(value = 9, message = "Maximum nine passengers allowed")
    Integer passengers,

    @NotEmpty(message = "Passenger details are required")
    @Size(max = 9, message = "Maximum nine passengers allowed")
    List<@NotNull @Valid PassengerRequest> passengerDetails

) {
    // Compatibilidade com testes que usam o construtor antigo.
    // Não cria dados fictícios de passageiros.
    public BookingRequest(
        Long flightId,
        Long returnFlightId,
        int passengers
    ) {
        this(flightId, returnFlightId, passengers, null);
    }
}
