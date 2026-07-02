package com.ar.crm2.exception;

/**
 * Generic proposal state violation that does not fit the narrow
 * {@link AccionStateTransitionException} (used by the entity state
 * machine itself).
 *
 * <p>Used at the application boundary when the requested operation
 * does not match the current proposal lifecycle state — for example,
 * confirming a proposal that is no longer PENDING because it was
 * superseded or already cancelled.
 *
 * <p>Maps to HTTP 409 Conflict at the REST boundary.
 */
public class AccionStateException extends DomainException {

    public AccionStateException(String message) {
        super(message);
    }

    /**
     * Build "La accion {accionId} no se puede {operacion} en estado {estadoActual}."
     */
    public static AccionStateException invalidState(String accionId, String operacion, String estadoActual) {
        return new AccionStateException(
                "La accion " + accionId + " no se puede " + operacion + " en estado " + estadoActual + "."
        );
    }
}