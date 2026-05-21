package com.ar.crm2.application.contacto.exception;

import java.util.UUID;

/**
 * Exception thrown when a Contacto cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class ContactoNotFoundException extends RuntimeException {

    private final UUID contactoId;

    private ContactoNotFoundException(UUID contactoId, String message) {
        super(message);
        this.contactoId = contactoId;
    }

    /**
     * Factory method with contextual id.
     */
    public static ContactoNotFoundException forId(UUID id) {
        return new ContactoNotFoundException(id, "Contacto no encontrado con id: " + id);
    }

    public UUID getContactoId() {
        return contactoId;
    }
}