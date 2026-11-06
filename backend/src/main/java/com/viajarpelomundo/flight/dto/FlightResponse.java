package com.viajarpelomundo.flight.dto;

import com.viajarpelomundo.flight.entity.Flight;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Define os dados que o cliente recebe ao pesquisar voos.
 * Evita expor directamente as entidades da base de dados.
 */
public record FlightResponse(
    Long id,
    String flightNumber,
    String airlineName,
    String airlineCode,
    String origin,
    String destination,
    OffsetDateTime departureTime,
    OffsetDateTime arrivalTime,
    long durationMinutes,
    BigDecimal basePrice,
    String currency,
    int availableSeats,
    String status
) {
    public static FlightResponse from(Flight flight) {
        return new FlightResponse(
            flight.getId(),
            flight.getFlightNumber(),
            flight.getAirline().getName(),
            flight.getAirline().getIataCode(),
            flight.getOriginAirport().getIataCode(),
            flight.getDestinationAirport().getIataCode(),
            flight.getDepartureTime(),
            flight.getArrivalTime(),
            Duration.between(
                flight.getDepartureTime(),
                flight.getArrivalTime()
            ).toMinutes(),
            flight.getBasePrice(),
            flight.getCurrency(),
            flight.getAvailableSeats(),
            flight.getStatus()
        );
    }
}
