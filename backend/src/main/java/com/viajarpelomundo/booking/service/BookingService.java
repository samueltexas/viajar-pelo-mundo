package com.viajarpelomundo.booking.service;

import com.viajarpelomundo.auth.repository.AppUserRepository;
import com.viajarpelomundo.booking.dto.BookingRequest;
import com.viajarpelomundo.booking.dto.BookingResponse;
import com.viajarpelomundo.booking.entity.Booking;
import com.viajarpelomundo.booking.repository.BookingRepository;
import com.viajarpelomundo.flight.repository.FlightRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Gere reservas fictícias.
 *
 * A transacção garante que o desconto dos lugares
 * e a criação da reserva acontecem em conjunto.
 */
@Service
public class BookingService {

    private final BookingRepository bookings;
    private final FlightRepository flights;
    private final AppUserRepository users;

    public BookingService(
        BookingRepository bookings,
        FlightRepository flights,
        AppUserRepository users
    ) {
        this.bookings = bookings;
        this.flights = flights;
        this.users = users;
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {

        if (request == null
            || request.flightId() == null
            || request.passengers() == null
            || request.passengers() < 1
            || request.passengers() > 9) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid booking request"
            );
        }

        // Actualização atómica: só desconta se houver lugares suficientes.
        int updated = flights.reserveSeats(
            request.flightId(),
            request.passengers()
        );

        if (updated == 0) {
            if (!flights.existsById(request.flightId())) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Flight not found"
                );
            }

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Flight unavailable or insufficient seats"
            );
        }

        var flight = flights.findById(request.flightId())
            .orElseThrow();

        var totalPrice = flight.getBasePrice()
            .multiply(java.math.BigDecimal.valueOf(request.passengers()));

        var booking = new Booking(
            UUID.randomUUID().toString(),
            flight,
            request.passengers(),
            totalPrice,
            flight.getCurrency()
        );

        // A reserva pertence ao utilizador autenticado.
        var email = SecurityContextHolder.getContext()
            .getAuthentication().getName();

        var user = users.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not found"
            ));

        booking.assignTo(user);

        return BookingResponse.from(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse findByReference(String reference) {
        var email = SecurityContextHolder.getContext()
            .getAuthentication().getName();

        return bookings.findByReferenceAndUser_Email(reference, email)
            .map(BookingResponse::from)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found"
            ));
    }
}
