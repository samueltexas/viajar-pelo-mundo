package com.viajarpelomundo.booking.dto;

import com.viajarpelomundo.booking.entity.Booking;
import java.math.BigDecimal;

public record BookingResponse(
    String reference,
    Long flightId,
    String flightNumber,
    Long returnFlightId,
    String returnFlightNumber,
    int passengers,
    BigDecimal totalPrice,
    String currency,
    String status
) {
    public static BookingResponse from(Booking booking) {
        var outbound = booking.getFlight();
        var inbound = booking.getReturnFlight();

        return new BookingResponse(
            booking.getReference(),
            outbound.getId(),
            outbound.getFlightNumber(),
            inbound == null ? null : inbound.getId(),
            inbound == null ? null : inbound.getFlightNumber(),
            booking.getPassengerCount(),
            booking.getTotalPrice(),
            booking.getCurrency(),
            booking.getStatus()
        );
    }
}
