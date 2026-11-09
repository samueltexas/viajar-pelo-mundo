package com.viajarpelomundo.booking.dto;

import com.viajarpelomundo.booking.entity.Booking;
import com.viajarpelomundo.booking.entity.BookingPassenger;

import java.math.BigDecimal;
import java.util.List;

public record BookingResponse(
    String reference,
    Long flightId,
    String flightNumber,
    Long returnFlightId,
    String returnFlightNumber,
    int passengers,
    BigDecimal totalPrice,
    String currency,
    String status,
    List<PassengerResponse> passengerDetails
) {

    // Construtor compatível com os serviços anteriores.
    public BookingResponse(
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
        this(
            reference,
            flightId,
            flightNumber,
            returnFlightId,
            returnFlightNumber,
            passengers,
            totalPrice,
            currency,
            status,
            List.of()
        );
    }

    // Mantém a compatibilidade com os serviços existentes.
    public static BookingResponse from(Booking booking) {
        return from(booking, List.of());
    }

    public static BookingResponse from(
        Booking booking,
        List<BookingPassenger> passengers
    ) {
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
            booking.getStatus(),
            passengers.stream()
                .map(PassengerResponse::from)
                .toList()
        );
    }
}
