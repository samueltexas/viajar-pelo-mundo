package com.viajarpelomundo.flight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Garante a disponibilidade do inventário de voos
 * quando o backend termina a inicialização.
 *
 * A geração é idempotente:
 * voos existentes não são alterados.
 */
@Component
public class FlightInventoryStartup {

    private static final Logger logger =
            LoggerFactory.getLogger(FlightInventoryStartup.class);

    private final FlightInventoryGenerator generator;

    public FlightInventoryStartup(FlightInventoryGenerator generator) {
        this.generator = generator;
    }

    /**
     * Executa depois de o Spring Boot estar pronto.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeFlightInventory() {

        logger.info("Checking the next 90 days of flight inventory...");

        int createdFlights = generator.generateNext90Days();

        logger.info(
                "Flight inventory initialization completed. New flights: {}",
                createdFlights
        );
    }
}
