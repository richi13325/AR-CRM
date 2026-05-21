package com.ar.crm2.application.trato.exception;

import java.util.UUID;

/**
 * Exception thrown when a Trato cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class TratoNotFoundException extends RuntimeException {

    private final UUID tratoId;

    private TratoNotFoundException(UUID tratoId, String message) {
        super(message);
        this.tratoId = tratoId;
    }

    /**
     * Factory method with contextual id.
     */
    public static TratoNotFoundException forId(UUID id) {
        return new TratoNotFoundException(id, "Trato no encontrado con id: " + id);
    }

    public UUID getTratoId() {
        return tratoId;
    }
}