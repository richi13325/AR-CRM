package com.ar.crm2.exception;

/**
 * Exception thrown when an actor attempts to operate on an AI action
 * proposal that was not requested by them.
 *
 * <p>Maps to HTTP 403 Forbidden at the REST boundary.
 */
public class AccionNotOwnedByActorException extends DomainException {

    public AccionNotOwnedByActorException(String message) {
        super(message);
    }

    /**
     * Build "El usuario {actorUsuarioId} no es el solicitante de la accion {accionId}."
     */
    public static AccionNotOwnedByActorException notRequester(String actorUsuarioId, String accionId) {
        return new AccionNotOwnedByActorException(
                "El usuario " + actorUsuarioId + " no es el solicitante de la accion " + accionId + "."
        );
    }
}