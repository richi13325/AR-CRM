package com.ar.crm2.application.ficha.exception;

import java.util.UUID;

/**
 * Exception thrown when a Ficha cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class FichaNotFoundException extends RuntimeException {

    private final UUID fichaId;

    private FichaNotFoundException(UUID fichaId, String message) {
        super(message);
        this.fichaId = fichaId;
    }

    /**
     * Factory method with contextual id.
     */
    public static FichaNotFoundException forId(UUID id) {
        return new FichaNotFoundException(id, "Ficha no encontrada con id: " + id);
    }

    public UUID getFichaId() {
        return fichaId;
    }
}