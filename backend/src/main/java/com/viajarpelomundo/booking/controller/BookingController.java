package com.viajarpelomundo.booking.controller;

import com.viajarpelomundo.booking.dto.BookingRequest;
import com.viajarpelomundo.booking.dto.BookingResponse;
import com.viajarpelomundo.booking.service.BookingService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * API para criar e consultar reservas fictícias.
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody BookingRequest request) {
        return service.create(request);
    }

    @GetMapping("/{reference}")
    public BookingResponse findByReference(
        @PathVariable String reference
    ) {
        return service.findByReference(reference);
    }
}
