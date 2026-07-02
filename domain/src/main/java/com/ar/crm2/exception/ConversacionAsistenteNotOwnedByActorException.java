package com.ar.crm2.exception;

/**
 * Exception thrown when an actor attempts to operate on an AI
 * conversation that was not started by them (actor mismatch) or that
 * belongs to a different tenant (empresa mismatch).
 *
 * <p>Mirrors {@link AccionNotOwnedByActorException} for the AI
 * conversation aggregate. Both exceptions map to HTTP 403 Forbidden
 * at the REST boundary.
 */
public class ConversacionAsistenteNotOwnedByActorException extends DomainException {

    public ConversacionAsistenteNotOwnedByActorException(String message) {
        super(message);
    }

    public static ConversacionAsistenteNotOwnedByActorException notOwner(
            String actorUsuarioId, String aiConversacionId
    ) {
        return new ConversacionAsistenteNotOwnedByActorException(
                "El usuario " + actorUsuarioId
                        + " no es el iniciador de la conversacion asistente " + aiConversacionId + "."
        );
    }

    public static ConversacionAsistenteNotOwnedByActorException tenantMismatch(
            String actorUsuarioId, String aiConversacionId
    ) {
        return new ConversacionAsistenteNotOwnedByActorException(
                "La conversacion asistente " + aiConversacionId
                        + " no pertenece a la empresa del usuario " + actorUsuarioId + "."
        );
    }
}