package com.ar.crm2.application.usuario.exception;

import java.util.UUID;

/**
 * Exception thrown when a Usuario cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class UsuarioNotFoundException extends RuntimeException {

    private final UUID usuarioId;

    private UsuarioNotFoundException(UUID usuarioId, String message) {
        super(message);
        this.usuarioId = usuarioId;
    }

    /**
     * Factory method with contextual id.
     */
    public static UsuarioNotFoundException forId(UUID id) {
        return new UsuarioNotFoundException(id, "Usuario no encontrado con id: " + id);
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }
}