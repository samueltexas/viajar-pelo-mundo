package com.viajarpelomundo.airline.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Representa uma companhia aérea fictícia.
 * Cada companhia pode operar vários voos.
 */
@Entity
@Table(name = "airlines")
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, unique = true, length = 2)
    private String iataCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Airline() {
    }

    public Long getId() {
        return id;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
