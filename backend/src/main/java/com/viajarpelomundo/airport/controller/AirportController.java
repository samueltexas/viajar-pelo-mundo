package com.viajarpelomundo.airport.controller;

import com.viajarpelomundo.airport.dto.AirportResponse;
import com.viajarpelomundo.airport.service.AirportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping
    public List<AirportResponse> getAirports(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country
    ) {
        if (city != null && !city.isBlank()) {
            return airportService.findByCity(city.trim());
        }

        if (country != null && !country.isBlank()) {
            return airportService.findByCountry(country.trim());
        }

        return airportService.findAll();
    }
}
