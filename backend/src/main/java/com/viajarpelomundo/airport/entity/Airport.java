package com.viajarpelomundo.airport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "airports")
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, unique = true, length = 3)
    private String iataCode;

    @Column(name = "icao_code", unique = true, length = 4)
    private String icaoCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Airport() {
    }

    public Long getId() {
        return id;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getIcaoCode() {
        return icaoCode;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
