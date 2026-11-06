package com.viajarpelomundo.airline.repository;

import com.viajarpelomundo.airline.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Permite consultar as companhias aéreas na base de dados.
 */
public interface AirlineRepository extends JpaRepository<Airline, Long> {

    Optional<Airline> findByIataCodeIgnoreCase(String iataCode);
}
