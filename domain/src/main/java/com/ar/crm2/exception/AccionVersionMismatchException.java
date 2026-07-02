package com.ar.crm2.exception;

/**
 * Exception thrown when the {@code expectedVersion} supplied on a
 * confirmation or rejection request does not match the proposal's
 * current optimistic-lock version.
 *
 * <p>Maps to HTTP 409 Conflict at the REST boundary.
 */
public class AccionVersionMismatchException extends DomainException {

    public AccionVersionMismatchException(String message) {
        super(message);
    }

    /**
     * Build "La accion {accionId} esperaba la versión {expected} pero estaba en {actual}."
     */
    public static AccionVersionMismatchException mismatch(String accionId, int expected, int actual) {
        return new AccionVersionMismatchException(
                "La accion " + accionId + " esperaba la versión " + expected + " pero estaba en " + actual + "."
        );
    }
}