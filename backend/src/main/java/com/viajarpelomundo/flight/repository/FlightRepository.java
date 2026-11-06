package com.viajarpelomundo.flight.repository;

import com.viajarpelomundo.flight.entity.Flight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

/**
 * Repositório responsável pelas consultas de voos.
 */
public interface FlightRepository extends JpaRepository<Flight, Long> {

    /**
     * Desconta lugares directamente no PostgreSQL.
     * A condição impede reservas acima da capacidade disponível.
     */
    @org.springframework.data.jpa.repository.Modifying(
        flushAutomatically = true,
        clearAutomatically = true
    )
    @Query(value = """
        UPDATE flights
        SET available_seats = available_seats - :passengers
        WHERE id = :flightId
          AND status = 'SCHEDULED'
          AND departure_time > CURRENT_TIMESTAMP
          AND available_seats >= :passengers
        """, nativeQuery = true)
    int reserveSeats(
        @Param("flightId") Long flightId,
        @Param("passengers") int passengers
    );


    @EntityGraph(attributePaths = {
        "airline",
        "originAirport",
        "destinationAirport"
    })
    @Query("""
        SELECT f FROM Flight f
        WHERE f.originAirport.iataCode = :origin
          AND f.destinationAirport.iataCode = :destination
          AND f.departureTime >= :start
          AND f.departureTime < :end
          AND f.status = 'SCHEDULED'
          AND f.availableSeats >= :passengers
        """)
    Page<Flight> search(
        @Param("origin") String origin,
        @Param("destination") String destination,
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end,
        @Param("passengers") int passengers,
        Pageable pageable
    );
}
