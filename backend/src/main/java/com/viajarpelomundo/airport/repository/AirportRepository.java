package com.viajarpelomundo.airport.repository;

import com.viajarpelomundo.airport.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    List<Airport> findByCityContainingIgnoreCase(String city);

    List<Airport> findByCountryCodeIgnoreCase(String countryCode);
}
