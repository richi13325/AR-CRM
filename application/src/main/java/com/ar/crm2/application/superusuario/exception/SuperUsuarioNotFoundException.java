package com.ar.crm2.application.superusuario.exception;

import java.util.UUID;

/**
 * Exception thrown when a SuperUsuario cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class SuperUsuarioNotFoundException extends RuntimeException {

    private final UUID superUsuarioId;

    private SuperUsuarioNotFoundException(UUID superUsuarioId, String message) {
        super(message);
        this.superUsuarioId = superUsuarioId;
    }

    /**
     * Factory method with contextual id.
     */
    public static SuperUsuarioNotFoundException forId(UUID id) {
        return new SuperUsuarioNotFoundException(id, "SuperUsuario no encontrado con id: " + id);
    }

    public UUID getSuperUsuarioId() {
        return superUsuarioId;
    }
}