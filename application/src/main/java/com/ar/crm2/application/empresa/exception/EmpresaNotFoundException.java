package com.ar.crm2.application.empresa.exception;

import java.util.UUID;

/**
 * Exception thrown when an Empresa cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class EmpresaNotFoundException extends RuntimeException {

    private final UUID empresaId;

    private EmpresaNotFoundException(UUID empresaId, String message) {
        super(message);
        this.empresaId = empresaId;
    }

    /**
     * Factory method with contextual id.
     */
    public static EmpresaNotFoundException forId(UUID id) {
        return new EmpresaNotFoundException(id, "Empresa not found with id: " + id);
    }

    public UUID getEmpresaId() {
        return empresaId;
    }
}