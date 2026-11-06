package com.viajarpelomundo.airport.dto;

import com.viajarpelomundo.airport.entity.Airport;

public record AirportResponse(
        Long id,
        String iataCode,
        String icaoCode,
        String name,
        String city,
        String countryCode
) {
    public static AirportResponse from(Airport airport) {
        return new AirportResponse(
                airport.getId(),
                airport.getIataCode(),
                airport.getIcaoCode(),
                airport.getName(),
                airport.getCity(),
                airport.getCountryCode()
        );
    }
}
