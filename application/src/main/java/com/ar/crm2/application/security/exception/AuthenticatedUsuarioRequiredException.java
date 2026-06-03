package com.ar.crm2.application.security.exception;

public class AuthenticatedUsuarioRequiredException extends RuntimeException {

    public AuthenticatedUsuarioRequiredException(String message) {
        super(message);
    }

    public static AuthenticatedUsuarioRequiredException forMissingUsuarioId() {
        return new AuthenticatedUsuarioRequiredException(
                "usuarioId not found in actor context — ensure the JWT contains the usuario_id claim");
    }

    public static AuthenticatedUsuarioRequiredException forMissingActorContext() {
        return new AuthenticatedUsuarioRequiredException(
                "authenticated actor context not found — ensure the request is authenticated");
    }
}
