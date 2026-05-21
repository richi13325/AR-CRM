package com.ar.crm2.application.tablero.exception;

import java.util.UUID;

/**
 * Exception thrown when a Tablero cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class TableroNotFoundException extends RuntimeException {

    private final UUID tableroId;

    private TableroNotFoundException(UUID tableroId, String message) {
        super(message);
        this.tableroId = tableroId;
    }

    /**
     * Factory method with contextual id.
     */
    public static TableroNotFoundException forId(UUID id) {
        return new TableroNotFoundException(id, "Tablero no encontrado con id: " + id);
    }

    public UUID getTableroId() {
        return tableroId;
    }
}