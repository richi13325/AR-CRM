package com.ar.crm2.exception;

/**
 * Exception thrown when an attempt is made to operate on an AI action
 * proposal that has passed its expiry time.
 *
 * <p>Maps to HTTP 409 Conflict at the REST boundary.
 */
public class AccionExpiredException extends DomainException {

    public AccionExpiredException(String message) {
        super(message);
    }

    /**
     * Build "La accion {accionId} expiró a las {expiresAt}."
     */
    public static AccionExpiredException expired(String accionId, String expiresAt) {
        return new AccionExpiredException(
                "La accion " + accionId + " expiró a las " + expiresAt + "."
        );
    }
}