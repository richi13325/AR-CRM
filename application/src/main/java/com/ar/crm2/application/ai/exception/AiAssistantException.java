package com.ar.crm2.application.ai.exception;

/**
 * Generic AI assistant failure raised when the underlying provider
 * fails or times out, or when the application layer cannot marshal
 * the result.
 *
 * <p>Maps to HTTP 502 Bad Gateway at the REST boundary.
 */
public class AiAssistantException extends RuntimeException {

    private AiAssistantException(String message) {
        super(message);
    }

    private AiAssistantException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AiAssistantException upstreamFailure(String reason) {
        return new AiAssistantException("El asistente de IA falló: " + reason + ".");
    }

    public static AiAssistantException upstreamFailure(String reason, Throwable cause) {
        return new AiAssistantException("El asistente de IA falló: " + reason + ".", cause);
    }

    public static AiAssistantException invalidAssistantOutput(String reason) {
        return new AiAssistantException("La salida del asistente es inválida: " + reason + ".");
    }
}