package com.ar.crm2.application.ai.exception;

/**
 * Thrown when a RegistrarAccion command is rejected because
 * of invalid arguments or out-of-scope tenant ownership.
 *
 * <p>Maps to HTTP 400 Bad Request when the issue is malformed input,
 * and to HTTP 403 Forbidden when the issue is tenant scope. The
 * GlobalExceptionHandler distinguishes via factory method name.
 */
public class AccionInvalidaException extends RuntimeException {

    private AccionInvalidaException(String message) {
        super(message);
    }

    public static AccionInvalidaException forInvalidInput(String reason) {
        return new AccionInvalidaException(
                "Comando de accion inválido: " + reason + "."
        );
    }

    public static AccionInvalidaException forTenantMismatch(String actorUsuarioId, String empresaId) {
        return new AccionInvalidaException(
                "La empresa " + empresaId + " no pertenece al usuario " + actorUsuarioId + "."
        );
    }
}