package com.ar.crm2.application.columna.exception;

import java.util.UUID;

/**
 * Exception thrown when a Columna cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class ColumnaNotFoundException extends RuntimeException {

    private final UUID columnaId;

    private ColumnaNotFoundException(UUID columnaId, String message) {
        super(message);
        this.columnaId = columnaId;
    }

    /**
     * Factory method with contextual id.
     */
    public static ColumnaNotFoundException forId(UUID id) {
        return new ColumnaNotFoundException(id, "Columna no encontrada con id: " + id);
    }

    public UUID getColumnaId() {
        return columnaId;
    }
}