package com.viajarpelomundo.airport.service;

import com.viajarpelomundo.airport.dto.AirportResponse;
import com.viajarpelomundo.airport.repository.AirportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AirportService {

    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<AirportResponse> findAll() {
        return airportRepository.findAll()
                .stream()
                .map(AirportResponse::from)
                .toList();
    }

    public List<AirportResponse> findByCity(String city) {
        return airportRepository.findByCityContainingIgnoreCase(city)
                .stream()
                .map(AirportResponse::from)
                .toList();
    }

    public List<AirportResponse> findByCountry(String countryCode) {
        return airportRepository.findByCountryCodeIgnoreCase(countryCode)
                .stream()
                .map(AirportResponse::from)
                .toList();
    }
}
