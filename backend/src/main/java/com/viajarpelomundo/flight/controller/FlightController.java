package com.viajarpelomundo.flight.controller;

import com.viajarpelomundo.flight.dto.FlightResponse;
import com.viajarpelomundo.flight.service.FlightSearchService;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Disponibiliza a pesquisa de voos através de uma API REST.
 */
@RestController
@RequestMapping("/api/v1/flights")
public class FlightController {

    private final FlightSearchService service;

    public FlightController(FlightSearchService service) {
        this.service = service;
    }

    @GetMapping
    public Page<FlightResponse> search(
        @RequestParam String origin,
        @RequestParam String destination,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,
        @RequestParam(defaultValue = "1") int passengers,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return service.search(
            origin,
            destination,
            date,
            passengers,
            page,
            size
        );
    }
}
