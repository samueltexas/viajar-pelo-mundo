package com.viajarpelomundo.flight.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Gera automaticamente o inventário futuro de voos fictícios.
 *
 * As rotas são configuradas na tabela flight_route_templates.
 * Os horários seguem UTC.
 *
 * Não altera voos existentes nem reservas.
 */
@Service
public class FlightInventoryGenerator {

    private final JdbcTemplate jdbcTemplate;

    public FlightInventoryGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executa diariamente às 02:00 UTC.
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    @Transactional
    public void scheduledGeneration() {
        generateNext90Days();
    }

    /**
     * Gera voos para os próximos 90 dias.
     *
     * A restrição de unicidade da V13 impede duplicados.
     */
    @Transactional
    public int generateNext90Days() {

        LocalDate startDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate endDate = startDate.plusDays(89);

        String sql = """
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
            SELECT
                route.flight_number,
                route.airline_id,
                route.origin_airport_id,
                route.destination_airport_id,
                (
                    days.day::timestamp
                    + make_interval(hours => route.departure_hour)
                ) AT TIME ZONE 'UTC',
                (
                    days.day::timestamp
                    + make_interval(
                        hours => route.departure_hour
                            + route.duration_hours
                    )
                ) AT TIME ZONE 'UTC',
                route.base_price,
                route.currency,
                route.total_seats,
                route.total_seats,
                'SCHEDULED'
            FROM flight_route_templates route
            CROSS JOIN generate_series(
                ?::date,
                ?::date,
                interval '1 day'
            ) AS days(day)
            WHERE route.active = TRUE
            ON CONFLICT (
                flight_number,
                departure_time
            ) DO NOTHING
            """;

        return jdbcTemplate.update(
            sql,
            startDate.toString(),
            endDate.toString()
        );
    }
}
