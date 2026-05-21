package com.ar.crm2.application.columna.exception;

import java.util.UUID;

/**
 * Exception thrown when a Columna has associated Fichas and cannot be deleted.
 * Maps to HTTP 409 Conflict.
 */
public class ColumnaHasAssociatedFichasException extends RuntimeException {

    private final UUID columnaId;

    private ColumnaHasAssociatedFichasException(UUID columnaId, String message) {
        super(message);
        this.columnaId = columnaId;
    }

    /**
     * Factory method with contextual id.
     */
    public static ColumnaHasAssociatedFichasException forId(UUID id) {
        return new ColumnaHasAssociatedFichasException(id, "Columna tiene Fichas asociadas y no puede ser eliminada: " + id);
    }

    public UUID getColumnaId() {
        return columnaId;
    }
}