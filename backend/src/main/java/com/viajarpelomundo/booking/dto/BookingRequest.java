package com.viajarpelomundo.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
    @NotNull(message = "Outbound flight is required")
    Long flightId,

    Long returnFlightId,

    @NotNull(message = "Passenger count is required")
    @Min(value = 1, message = "At least one passenger is required")
    @Max(value = 9, message = "Maximum nine passengers allowed")
    Integer passengers
) {
}
