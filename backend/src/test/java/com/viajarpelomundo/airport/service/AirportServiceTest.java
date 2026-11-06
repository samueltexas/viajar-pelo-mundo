package com.viajarpelomundo.airport.service;

import com.viajarpelomundo.airport.entity.Airport;
import com.viajarpelomundo.airport.repository.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AirportServiceTest {

    private AirportRepository airportRepository;
    private AirportService airportService;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        airportService = new AirportService(airportRepository);
    }

    @Test
    void shouldFindAirportsByCity() throws Exception {
        Airport airport = createAirport(
                1L,
                "WAW",
                "Warsaw Chopin Airport",
                "Warsaw",
                "PL"
        );

        when(airportRepository.findByCityContainingIgnoreCase("Warsaw"))
                .thenReturn(List.of(airport));

        var result = airportService.findByCity("Warsaw");

        assertEquals(1, result.size());
        assertEquals("WAW", result.getFirst().iataCode());
        assertEquals("Warsaw", result.getFirst().city());

        verify(airportRepository)
                .findByCityContainingIgnoreCase("Warsaw");
    }

    @Test
    void shouldReturnEmptyListWhenCountryHasNoAirports() {
        when(airportRepository.findByCountryCodeIgnoreCase("XX"))
                .thenReturn(List.of());

        var result = airportService.findByCountry("XX");

        assertTrue(result.isEmpty());

        verify(airportRepository)
                .findByCountryCodeIgnoreCase("XX");
    }

    private Airport createAirport(
            Long id,
            String iataCode,
            String name,
            String city,
            String countryCode
    ) throws Exception {
        var constructor = Airport.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Airport airport = constructor.newInstance();

        setField(airport, "id", id);
        setField(airport, "iataCode", iataCode);
        setField(airport, "name", name);
        setField(airport, "city", city);
        setField(airport, "countryCode", countryCode);

        return airport;
    }

    private void setField(
            Airport airport,
            String fieldName,
            Object value
    ) throws Exception {
        Field field = Airport.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(airport, value);
    }
}
