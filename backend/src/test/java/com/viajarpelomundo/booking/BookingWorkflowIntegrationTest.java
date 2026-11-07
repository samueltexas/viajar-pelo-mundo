package com.viajarpelomundo.booking;

import com.viajarpelomundo.booking.dto.BookingRequest;
import com.viajarpelomundo.booking.service.BookingService;
import com.viajarpelomundo.booking.management.BookingManagementService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingWorkflowIntegrationTest {

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingManagementService managementService;

    @Autowired
    JdbcTemplate jdbc;

    private final List<Long> flightIds = new ArrayList<>();
    private final List<Long> userIds = new ArrayList<>();

    private String ownerEmail;
    private String otherEmail;
    private Long outboundId;
    private Long returnId;

    private Long insertId(String sql, Object... params) {
        Long id = jdbc.queryForObject(sql, Long.class, params);
        assertNotNull(id);
        return id;
    }

    private Long createUser(String email) {
        Long id = insertId("""
            INSERT INTO app_users (email, password_hash, role)
            VALUES (?, ?, 'CUSTOMER')
            RETURNING id
            """, email, "integration-test-not-a-real-password");

        userIds.add(id);
        return id;
    }

    private Long createFlight(
        Long origin,
        Long destination,
        OffsetDateTime departure,
        int seats,
        BigDecimal price
    ) {
        Long airline = jdbc.queryForObject(
            "SELECT id FROM airlines ORDER BY id LIMIT 1",
            Long.class
        );

        assertNotNull(airline);

        String number = "T" + UUID.randomUUID()
            .toString().replace("-", "")
            .substring(0, 8).toUpperCase();

        Long id = insertId("""
            INSERT INTO flights (
                flight_number, airline_id,
                origin_airport_id, destination_airport_id,
                departure_time, arrival_time,
                base_price, currency,
                total_seats, available_seats, status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, 'EUR', 10, ?, 'SCHEDULED')
            RETURNING id
            """,
            number, airline, origin, destination,
            departure, departure.plusHours(2),
            price, seats
        );

        flightIds.add(id);
        return id;
    }

    private void authenticate(String email) {
        var authentication =
            new UsernamePasswordAuthenticationToken(
                email, null, List.of()
            );

        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }

    private int seats(Long id) {
        Integer result = jdbc.queryForObject(
            "SELECT available_seats FROM flights WHERE id = ?",
            Integer.class, id
        );
        assertNotNull(result);
        return result;
    }

    private long bookingsForUser(String email) {
        Long count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM bookings b
            JOIN app_users u ON u.id = b.user_id
            WHERE u.email = ?
            """, Long.class, email);

        assertNotNull(count);
        return count;
    }

    @BeforeEach
    void setup() {
        String suffix = UUID.randomUUID()
            .toString().replace("-", "");

        ownerEmail = "owner-" + suffix + "@example.test";
        otherEmail = "other-" + suffix + "@example.test";

        createUser(ownerEmail);
        createUser(otherEmail);

        List<Long> airports = jdbc.queryForList(
            "SELECT id FROM airports ORDER BY id LIMIT 2",
            Long.class
        );

        assertEquals(2, airports.size(),
            "Test database requires two airports");

        OffsetDateTime departure =
            OffsetDateTime.now().plusDays(40);

        outboundId = createFlight(
            airports.get(0), airports.get(1),
            departure, 5, new BigDecimal("100.00")
        );

        returnId = createFlight(
            airports.get(1), airports.get(0),
            departure.plusDays(7), 5,
            new BigDecimal("150.00")
        );

        authenticate(ownerEmail);
    }

    @AfterEach
    void cleanup() {
        try {
            for (Long userId : userIds) {
                jdbc.update("""
                    DELETE FROM demo_payments
                    WHERE booking_id IN (
                        SELECT id FROM bookings WHERE user_id = ?
                    )
                    """, userId);

                jdbc.update(
                    "DELETE FROM bookings WHERE user_id = ?",
                    userId
                );
            }

            for (Long flightId : flightIds) {
                jdbc.update(
                    "DELETE FROM flights WHERE id = ?",
                    flightId
                );
            }

            for (Long userId : userIds) {
                jdbc.update(
                    "DELETE FROM app_users WHERE id = ?",
                    userId
                );
            }
        } finally {
            SecurityContextHolder.clearContext();
            flightIds.clear();
            userIds.clear();
        }
    }

    @Test
    void shouldCreateRoundTripWithCorrectPrice() {
        var response = bookingService.create(
            new BookingRequest(outboundId, returnId, 2)
        );

        assertEquals("CONFIRMED", response.status());
        assertEquals(outboundId, response.flightId());
        assertEquals(returnId, response.returnFlightId());
        assertEquals(2, response.passengers());

        assertEquals(
            0,
            new BigDecimal("500.00")
                .compareTo(response.totalPrice())
        );

        assertEquals(3, seats(outboundId));
        assertEquals(3, seats(returnId));
        assertEquals(1, bookingsForUser(ownerEmail));
    }

    @Test
    void shouldRollbackOutboundWhenReturnIsUnavailable() {
        jdbc.update(
            "UPDATE flights SET available_seats = 0 WHERE id = ?",
            returnId
        );

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> bookingService.create(
                new BookingRequest(outboundId, returnId, 2)
            )
        );

        assertEquals(
            HttpStatus.CONFLICT,
            exception.getStatusCode()
        );

        assertEquals(
            5, seats(outboundId),
            "Outbound seats must be rolled back"
        );

        assertEquals(0, seats(returnId));
        assertEquals(0, bookingsForUser(ownerEmail));
    }

    @Test
    void shouldCancelRoundTripAndRestoreSeats() {
        var booking = bookingService.create(
            new BookingRequest(outboundId, returnId, 2)
        );

        assertEquals(3, seats(outboundId));
        assertEquals(3, seats(returnId));

        var cancelled = managementService.cancel(
            booking.reference()
        );

        assertEquals("CANCELLED", cancelled.status());
        assertEquals(5, seats(outboundId));
        assertEquals(5, seats(returnId));
    }

    @Test
    void shouldRejectDuplicateCancellation() {
        var booking = bookingService.create(
            new BookingRequest(outboundId, returnId, 2)
        );

        managementService.cancel(booking.reference());

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> managementService.cancel(booking.reference())
        );

        assertEquals(
            HttpStatus.CONFLICT,
            exception.getStatusCode()
        );

        assertEquals(5, seats(outboundId));
        assertEquals(5, seats(returnId));
    }

    @Test
    void shouldProtectBookingFromAnotherUser() {
        var booking = bookingService.create(
            new BookingRequest(outboundId, returnId, 2)
        );

        authenticate(otherEmail);

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> managementService.cancel(booking.reference())
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        assertEquals(3, seats(outboundId));
        assertEquals(3, seats(returnId));

        authenticate(ownerEmail);

        assertEquals(
            "CONFIRMED",
            bookingService.findByReference(
                booking.reference()
            ).status()
        );
    }
}
