package com.ar.crm2.application.contacto.exception;

import java.util.UUID;

/**
 * Exception thrown when a Contacto has associated Tratos and cannot be deleted.
 * Maps to HTTP 409 Conflict.
 */
public class ContactoHasAssociatedTratosException extends RuntimeException {

    private final UUID contactoId;

    private ContactoHasAssociatedTratosException(UUID contactoId, String message) {
        super(message);
        this.contactoId = contactoId;
    }

    /**
     * Factory method with contextual id.
     */
    public static ContactoHasAssociatedTratosException forId(UUID id) {
        return new ContactoHasAssociatedTratosException(id, "Contacto tiene Tratos asociados y no puede ser eliminado: " + id);
    }

    public UUID getContactoId() {
        return contactoId;
    }
}