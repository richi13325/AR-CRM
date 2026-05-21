package com.ar.crm2.application.empresa.exception;

import java.util.UUID;

/**
 * Exception thrown when an Empresa has associated Tratos and cannot be deleted.
 * Maps to HTTP 409 Conflict.
 */
public class EmpresaHasAssociatedTratosException extends RuntimeException {

    private final UUID empresaId;

    private EmpresaHasAssociatedTratosException(UUID empresaId, String message) {
        super(message);
        this.empresaId = empresaId;
    }

    /**
     * Factory method with contextual id.
     */
    public static EmpresaHasAssociatedTratosException forId(UUID id) {
        return new EmpresaHasAssociatedTratosException(id, "Empresa has associated Tratos and cannot be deleted: " + id);
    }

    public UUID getEmpresaId() {
        return empresaId;
    }
}