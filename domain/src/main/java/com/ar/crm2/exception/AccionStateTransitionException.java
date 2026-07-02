package com.ar.crm2.exception;

/**
 * Exception thrown when a state transition on an AiAccion
 * violates business rules.
 *
 * <p>Used by the domain entity to reject attempts such as confirming a
 * REJECTED proposal or marking EXECUTED a PENDING one. Maps to HTTP 409
 * Conflict at the REST boundary (handled in GlobalExceptionHandler).
 */
public class AccionStateTransitionException extends DomainException {

    public AccionStateTransitionException(String message) {
        super(message);
    }

    public AccionStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Build "No se puede confirmar una accion en estado {estadoActual}."
     */
    public static AccionStateTransitionException transicionNoPermitida(String estadoActual, String accion) {
        return new AccionStateTransitionException(
                "No se puede " + accion + " una accion en estado " + estadoActual + "."
        );
    }

    /**
     * Build "La accion ya está en estado terminal {estadoActual}."
     */
    public static AccionStateTransitionException estadoTerminal(String estadoActual) {
        return new AccionStateTransitionException(
                "La accion ya está en estado terminal " + estadoActual + "."
        );
    }
}