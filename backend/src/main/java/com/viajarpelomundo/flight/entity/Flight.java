package com.viajarpelomundo.flight.entity;

import com.viajarpelomundo.airline.entity.Airline;
import com.viajarpelomundo.airport.entity.Airport;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Representa um voo guardado no PostgreSQL.
 *
 * Um voo pertence a uma companhia aérea e possui
 * um aeroporto de origem e outro de destino.
 */
@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    // Relação entre o voo e a companhia aérea.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;

    // Aeroporto onde o voo começa.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_airport_id", nullable = false)
    private Airport originAirport;

    // Aeroporto onde o voo termina.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private Airport destinationAirport;

    @Column(name = "departure_time", nullable = false)
    private OffsetDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private OffsetDateTime arrivalTime;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Flight() {
    }

    public Long getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public Airline getAirline() {
        return airline;
    }

    public Airport getOriginAirport() {
        return originAirport;
    }

    public Airport getDestinationAirport() {
        return destinationAirport;
    }

    public OffsetDateTime getDepartureTime() {
        return departureTime;
    }

    public OffsetDateTime getArrivalTime() {
        return arrivalTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
