package com.ar.crm2.exception;

import java.util.UUID;

/**
 * Exception thrown when an AI action proposal cannot be located by id.
 *
 * <p>Maps to HTTP 404 Not Found at the REST boundary (handled in
 * GlobalExceptionHandler during PR 4).
 */
public class AccionNotFoundException extends DomainException {

    public AccionNotFoundException(String message) {
        super(message);
    }

    /**
     * Build "No se encontró la accion de IA {id}."
     */
    public static AccionNotFoundException forId(UUID id) {
        return new AccionNotFoundException("No se encontró la accion de IA " + id + ".");
    }
}