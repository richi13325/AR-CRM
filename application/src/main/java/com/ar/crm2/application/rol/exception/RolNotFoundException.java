package com.ar.crm2.application.rol.exception;

import java.util.UUID;

/**
 * Exception thrown when a Rol cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class RolNotFoundException extends RuntimeException {

    private final UUID rolId;

    private RolNotFoundException(UUID rolId, String message) {
        super(message);
        this.rolId = rolId;
    }

    /**
     * Factory method with contextual id.
     */
    public static RolNotFoundException forId(UUID id) {
        return new RolNotFoundException(id, "Rol no encontrado con id: " + id);
    }

    public UUID getRolId() {
        return rolId;
    }
}