package com.ar.crm2.application.tarea.exception;

import java.util.UUID;

/**
 * Exception thrown when a Tarea cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class TareaNotFoundException extends RuntimeException {

    private final UUID tareaId;

    private TareaNotFoundException(UUID tareaId, String message) {
        super(message);
        this.tareaId = tareaId;
    }

    /**
     * Factory method with contextual id.
     */
    public static TareaNotFoundException forId(UUID id) {
        return new TareaNotFoundException(id, "Tarea no encontrada con id: " + id);
    }

    public UUID getTareaId() {
        return tareaId;
    }
}