package com.viajarpelomundo.flight.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FlightReservationIntegrationTest {

    @Autowired
    private FlightRepository flights;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<Long> createdFlightIds = new ArrayList<>();

    private Long createFlight(int availableSeats) {
        Long airlineId = jdbc.queryForObject(
            "SELECT id FROM airlines ORDER BY id LIMIT 1",
            Long.class
        );

        List<Long> airportIds = jdbc.queryForList(
            "SELECT id FROM airports ORDER BY id LIMIT 2",
            Long.class
        );

        assertNotNull(airlineId);
        assertEquals(2, airportIds.size(),
            "Two airports are required for integration tests");

        String flightNumber = "T" +
            UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        OffsetDateTime departure = OffsetDateTime.now().plusDays(30);

        Long id = jdbc.queryForObject("""
            INSERT INTO flights (
                flight_number,
                airline_id,
                origin_airport_id,
                destination_airport_id,
                departure_time,
                arrival_time,
                base_price,
                currency,
                total_seats,
                available_seats,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, 100.00, 'EUR', 10, ?, 'SCHEDULED')
            RETURNING id
            """,
            Long.class,
            flightNumber,
            airlineId,
            airportIds.get(0),
            airportIds.get(1),
            departure,
            departure.plusHours(2),
            availableSeats
        );

        assertNotNull(id);
        createdFlightIds.add(id);
        return id;
    }

    private int availableSeats(Long flightId) {
        Integer seats = jdbc.queryForObject(
            "SELECT available_seats FROM flights WHERE id = ?",
            Integer.class,
            flightId
        );

        assertNotNull(seats);
        return seats;
    }

    @AfterEach
    void cleanup() {
        for (Long id : createdFlightIds) {
            jdbc.update("DELETE FROM flights WHERE id = ?", id);
        }
        createdFlightIds.clear();
    }

    @Test
    void shouldReserveAvailableSeats() {
        Long flightId = createFlight(5);

        TransactionTemplate tx =
            new TransactionTemplate(transactionManager);

        int updated = tx.execute(status ->
            flights.reserveSeats(flightId, 3)
        );

        assertEquals(1, updated);
        assertEquals(2, availableSeats(flightId));
    }

    @Test
    void shouldRollbackSeatReservation() {
        Long flightId = createFlight(5);

        TransactionTemplate tx =
            new TransactionTemplate(transactionManager);

        assertThrows(IllegalStateException.class, () ->
            tx.execute(status -> {
                assertEquals(
                    1,
                    flights.reserveSeats(flightId, 3)
                );

                throw new IllegalStateException(
                    "Simulated failure after seat reservation"
                );
            })
        );

        assertEquals(
            5,
            availableSeats(flightId),
            "Rollback must restore the original seat count"
        );
    }

    @Test
    void shouldPreventOverbookingUnderConcurrency() throws Exception {
        Long flightId = createFlight(1);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            List<Future<Integer>> results = new ArrayList<>();

            for (int i = 0; i < 2; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                            "Start signal timed out"
                        );
                    }

                    TransactionTemplate tx =
                        new TransactionTemplate(transactionManager);

                    return tx.execute(status ->
                        flights.reserveSeats(flightId, 1)
                    );
                }));
            }

            assertTrue(
                ready.await(10, TimeUnit.SECONDS),
                "Both workers must be ready"
            );

            start.countDown();

            int successfulReservations = 0;

            for (Future<Integer> result : results) {
                successfulReservations +=
                    result.get(15, TimeUnit.SECONDS);
            }

            assertEquals(
                1,
                successfulReservations,
                "Only one transaction may reserve the final seat"
            );

            assertEquals(
                0,
                availableSeats(flightId),
                "Available seats must never become negative"
            );

        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS),
                "Worker threads must terminate"
            );
        }
    }
}
