package com.viajarpelomundo.booking.dto;

import com.viajarpelomundo.booking.entity.Booking;
import java.math.BigDecimal;

public record BookingResponse(
    String reference,
    Long flightId,
    String flightNumber,
    int passengers,
    BigDecimal totalPrice,
    String currency,
    String status
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
            booking.getReference(),
            booking.getFlight().getId(),
            booking.getFlight().getFlightNumber(),
            booking.getPassengerCount(),
            booking.getTotalPrice(),
            booking.getCurrency(),
            booking.getStatus()
        );
    }
}
