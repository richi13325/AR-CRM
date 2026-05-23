package com.ar.crm2.application.superusuario.command;

import java.util.UUID;

/**
 * Command to create a new SuperUsuario.
 * Required fields validated at construction time.
 * keycloakId is optional and may be null.
 * initialPassword is used only during Keycloak provisioning and is never persisted.
 */
public record CreateSuperUsuarioCommand(
    String correo,
    String keycloakId,
    String initialPassword
) {

    public CreateSuperUsuarioCommand {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalArgumentException("initialPassword is required");
        }
    }
}