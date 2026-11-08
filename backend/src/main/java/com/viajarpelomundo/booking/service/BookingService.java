package com.viajarpelomundo.booking.service;

import com.viajarpelomundo.auth.repository.AppUserRepository;
import com.viajarpelomundo.booking.dto.BookingRequest;
import com.viajarpelomundo.booking.dto.BookingResponse;
import com.viajarpelomundo.booking.dto.PassengerRequest;
import com.viajarpelomundo.booking.entity.Booking;
import com.viajarpelomundo.booking.entity.BookingPassenger;
import com.viajarpelomundo.booking.repository.BookingPassengerRepository;
import com.viajarpelomundo.booking.repository.BookingRepository;
import com.viajarpelomundo.flight.entity.Flight;
import com.viajarpelomundo.flight.repository.FlightRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final BookingPassengerRepository passengerRepository;
    private final FlightRepository flights;
    private final AppUserRepository users;

    public BookingService(
        BookingRepository bookings,
        BookingPassengerRepository passengerRepository,
        FlightRepository flights,
        AppUserRepository users
    ) {
        this.bookings = bookings;
        this.passengerRepository = passengerRepository;
        this.flights = flights;
        this.users = users;
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {

        validateRequest(request);

        Long outboundId = request.flightId();
        Long returnId = request.returnFlightId();
        int passengerCount = request.passengers();

        if (returnId != null && outboundId.equals(returnId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Outbound and return flights must be different"
            );
        }

        Flight outbound = findFlight(outboundId);
        Flight inbound = returnId == null
            ? null
            : findFlight(returnId);

        if (inbound != null) {
            validateRoundTrip(outbound, inbound);
        }

        // Ordem estável para reduzir o risco de deadlocks.
        List<Long> flightIds = inbound == null
            ? List.of(outboundId)
            : List.of(outboundId, returnId)
                .stream()
                .sorted()
                .toList();

        for (Long flightId : flightIds) {

            int updated = flights.reserveSeats(
                flightId,
                passengerCount
            );

            if (updated != 1) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Flight unavailable or insufficient seats: "
                        + flightId
                );
            }
        }

        BigDecimal totalPrice = outbound.getBasePrice();

        if (inbound != null) {
            totalPrice = totalPrice.add(
                inbound.getBasePrice()
            );
        }

        totalPrice = totalPrice.multiply(
            BigDecimal.valueOf(passengerCount)
        );

        String email = SecurityContextHolder
            .getContext()
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
            passengerCount,
            totalPrice,
            outbound.getCurrency()
        );

        booking.assignTo(user);

        Booking savedBooking = bookings.save(booking);

        // Os passageiros pertencem à mesma transacção.
        List<BookingPassenger> passengers =
            request.passengerDetails()
                .stream()
                .map(passenger -> new BookingPassenger(
                    savedBooking,
                    passenger.firstName().trim(),
                    passenger.lastName().trim(),
                    passenger.dateOfBirth()
                ))
                .toList();

        List<BookingPassenger> savedPassengers =
            passengerRepository.saveAll(passengers);

        return BookingResponse.from(
            savedBooking,
            savedPassengers
        );
    }

    private void validateRequest(BookingRequest request) {

        if (request == null
            || request.flightId() == null
            || request.passengers() == null
            || request.passengers() < 1
            || request.passengers() > 9
            || request.passengerDetails() == null
            || request.passengerDetails().size()
                != request.passengers()) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Passenger count must match passenger details"
            );
        }

        for (PassengerRequest passenger
            : request.passengerDetails()) {

            if (passenger == null
                || passenger.firstName() == null
                || passenger.firstName().isBlank()
                || passenger.firstName().trim().length() > 100
                || passenger.lastName() == null
                || passenger.lastName().isBlank()
                || passenger.lastName().trim().length() > 100
                || passenger.dateOfBirth() == null
                || passenger.dateOfBirth()
                    .isAfter(LocalDate.now())) {

                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid passenger details"
                );
            }
        }
    }

    private Flight findFlight(Long id) {
        return flights.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight not found: " + id
            ));
    }

    private void validateRoundTrip(
        Flight outbound,
        Flight inbound
    ) {

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

        if (!outbound.getCurrency().equals(
            inbound.getCurrency()
        )) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Both flights must use the same currency"
            );
        }
    }

    @Transactional(readOnly = true)
    public BookingResponse findByReference(
        String reference
    ) {

        String email = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        Booking booking = bookings
            .findByReferenceAndUser_Email(reference, email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found"
            ));

        List<BookingPassenger> passengers =
            passengerRepository.findByBooking_IdOrderByIdAsc(
                booking.getId()
            );

        return BookingResponse.from(booking, passengers);
    }
}
