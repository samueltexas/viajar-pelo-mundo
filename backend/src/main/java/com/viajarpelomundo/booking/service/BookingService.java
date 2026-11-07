package com.viajarpelomundo.booking.service;

import com.viajarpelomundo.auth.repository.AppUserRepository;
import com.viajarpelomundo.booking.dto.BookingRequest;
import com.viajarpelomundo.booking.dto.BookingResponse;
import com.viajarpelomundo.booking.entity.Booking;
import com.viajarpelomundo.booking.repository.BookingRepository;
import com.viajarpelomundo.flight.entity.Flight;
import com.viajarpelomundo.flight.repository.FlightRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

        Long outboundId = request.flightId();
        Long returnId = request.returnFlightId();
        int passengers = request.passengers();

        if (returnId != null && outboundId.equals(returnId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Outbound and return flights must be different"
            );
        }

        Flight outbound = findFlight(outboundId);
        Flight inbound = returnId == null ? null : findFlight(returnId);

        if (inbound != null) {
            validateRoundTrip(outbound, inbound);
        }

        // Actualizações em ordem estável reduzem deadlocks
        // quando diferentes utilizadores reservam os mesmos voos.
        List<Long> flightIds = inbound == null
            ? List.of(outboundId)
            : List.of(outboundId, returnId)
                .stream()
                .sorted()
                .toList();

        for (Long flightId : flightIds) {
            int updated = flights.reserveSeats(flightId, passengers);

            if (updated != 1) {
                // Esta excepção reverte também os descontos
                // realizados anteriormente na mesma transacção.
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Flight unavailable or insufficient seats: " + flightId
                );
            }
        }

        BigDecimal totalPrice = outbound.getBasePrice();

        if (inbound != null) {
            totalPrice = totalPrice.add(inbound.getBasePrice());
        }

        totalPrice = totalPrice.multiply(
            BigDecimal.valueOf(passengers)
        );

        String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        var user = users.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not found"
            ));

        Booking booking = new Booking(
            UUID.randomUUID().toString(),
            outbound,
            inbound,
            passengers,
            totalPrice,
            outbound.getCurrency()
        );

        booking.assignTo(user);

        return BookingResponse.from(bookings.save(booking));
    }

    private Flight findFlight(Long id) {
        return flights.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight not found: " + id
            ));
    }

    private void validateRoundTrip(Flight outbound, Flight inbound) {
        String outboundOrigin =
            outbound.getOriginAirport().getIataCode();
        String outboundDestination =
            outbound.getDestinationAirport().getIataCode();

        String returnOrigin =
            inbound.getOriginAirport().getIataCode();
        String returnDestination =
            inbound.getDestinationAirport().getIataCode();

        if (!outboundOrigin.equals(returnDestination)
            || !outboundDestination.equals(returnOrigin)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Return flight must reverse the outbound route"
            );
        }

        if (!inbound.getDepartureTime().isAfter(
            outbound.getArrivalTime()
        )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Return flight must depart after outbound arrival"
            );
        }

        if (!outbound.getCurrency().equals(inbound.getCurrency())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Both flights must use the same currency"
            );
        }
    }

    @Transactional(readOnly = true)
    public BookingResponse findByReference(String reference) {
        String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        return bookings.findByReferenceAndUser_Email(reference, email)
            .map(BookingResponse::from)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found"
            ));
    }
}
