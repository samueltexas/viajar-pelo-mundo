package com.viajarpelomundo.flight.service;

import com.viajarpelomundo.flight.dto.FlightResponse;
import com.viajarpelomundo.flight.repository.FlightRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;

/**
 * Serviço responsável pela pesquisa de voos directos.
 *
 * Valida os critérios introduzidos pelo utilizador,
 * consulta a base de dados e devolve resultados paginados.
 */
@Service
@Transactional(readOnly = true)
public class FlightSearchService {

    private final FlightRepository repository;

    public FlightSearchService(FlightRepository repository) {
        this.repository = repository;
    }

    public Page<FlightResponse> search(
        String origin,
        String destination,
        LocalDate date,
        int passengers,
        int page,
        int size
    ) {
        // Os códigos IATA dos aeroportos devem ter três letras.
        if (origin == null || !origin.matches("(?i)[A-Z]{3}")
            || destination == null || !destination.matches("(?i)[A-Z]{3}")) {
            throw new IllegalArgumentException(
                "Origin and destination must be valid IATA codes"
            );
        }

        origin = origin.toUpperCase(Locale.ROOT);
        destination = destination.toUpperCase(Locale.ROOT);

        // Impede que o utilizador pesquise um voo para o mesmo aeroporto.
        if (origin.equals(destination)) {
            throw new IllegalArgumentException(
                "Origin and destination must be different"
            );
        }

        // Limita a quantidade de passageiros e resultados por página.
        if (date == null || passengers < 1 || passengers > 9
            || page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                "Invalid search parameters"
            );
        }

        // Por enquanto, as datas dos voos demonstrativos usam UTC.
        var start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        var end = date.plusDays(1).atStartOfDay()
            .atOffset(ZoneOffset.UTC);

        var pageable = PageRequest.of(
            page,
            size,
            Sort.by("departureTime").ascending()
        );

        return repository.search(
            origin,
            destination,
            start,
            end,
            passengers,
            pageable
        ).map(FlightResponse::from);
    }
}
