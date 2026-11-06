package com.viajarpelomundo.booking.dto;

/**
 * Dados necessários para efectuar uma reserva fictícia.
 */
public record BookingRequest(
    Long flightId,
    Integer passengers
) {
}
