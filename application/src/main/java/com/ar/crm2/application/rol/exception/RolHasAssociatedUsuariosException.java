package com.ar.crm2.application.rol.exception;

import java.util.UUID;

/**
 * Exception thrown when a Rol has associated Usuarios and cannot be deleted.
 * Maps to HTTP 409 Conflict.
 */
public class RolHasAssociatedUsuariosException extends RuntimeException {

    private final UUID rolId;

    private RolHasAssociatedUsuariosException(UUID rolId, String message) {
        super(message);
        this.rolId = rolId;
    }

    /**
     * Factory method with contextual id.
     */
    public static RolHasAssociatedUsuariosException forId(UUID id) {
        return new RolHasAssociatedUsuariosException(id, "Rol tiene Usuarios asociados y no puede ser eliminado: " + id);
    }

    public UUID getRolId() {
        return rolId;
    }
}