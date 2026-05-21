package com.ar.crm2.exception;

/**
 * Exception thrown when a state transition on a Contacto violates business rules.
 */
public class ContactoStateTransitionException extends DomainException {

    public ContactoStateTransitionException(String message) {
        super(message);
    }

    public ContactoStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Build "No se puede volver a Prospecto desde el estado {estadoActual}."
     */
    public static ContactoStateTransitionException transicionAProspectoNoPermitida(String estadoActual) {
        return new ContactoStateTransitionException(
                "No se puede volver a Prospecto desde el estado " + estadoActual + "."
        );
    }

    /**
     * Build "No se puede marcar como inactivo un contacto con tratos activos."
     */
    public static ContactoStateTransitionException inactivoConTratosActivos() {
        return new ContactoStateTransitionException(
                "No se puede marcar como inactivo un contacto con tratos activos."
        );
    }
}