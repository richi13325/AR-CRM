package com.ar.crm2.application.ai.exception;

/**
 * Thrown when a conversation id referenced by an AI request does not
 * exist or is not accessible to the actor.
 *
 * <p>Maps to HTTP 404 Not Found at the REST boundary.
 */
public class ConversacionAsistenteNoEncontradaException extends RuntimeException {

    private ConversacionAsistenteNoEncontradaException(String message) {
        super(message);
    }

    public static ConversacionAsistenteNoEncontradaException forId(String id) {
        return new ConversacionAsistenteNoEncontradaException(
                "No se encontró la conversación de IA " + id + "."
        );
    }
}