package com.viajarpelomundo.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centraliza os erros da API.
 * Nunca devolvemos stack traces ou detalhes internos ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleStatus(
        ResponseStatusException exception,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(
            exception.getStatusCode().value()
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            exception.getStatusCode(),
            exception.getReason() == null
                ? "Request could not be completed"
                : exception.getReason()
        );

        problem.setTitle(status == null
            ? "Request error"
            : status.getReasonPhrase());

        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(exception.getStatusCode())
            .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
        Exception exception,
        HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected server error occurred"
        );

        problem.setTitle("Internal Server Error");
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(
            HttpStatus.INTERNAL_SERVER_ERROR
        ).body(problem);
    }
}
