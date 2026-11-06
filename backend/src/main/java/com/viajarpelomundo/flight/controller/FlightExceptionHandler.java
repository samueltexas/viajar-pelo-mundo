package com.viajarpelomundo.flight.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Transforma erros de validação em respostas HTTP adequadas.
 */
@RestControllerAdvice(assignableTypes = FlightController.class)
public class FlightExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidSearch(
        IllegalArgumentException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            exception.getMessage()
        );
    }
}
