package com.ar.crm2.application.usuario.command;

import java.util.UUID;

/**
 * Command to create a new Usuario.
 * Required fields validated at construction time.
 * keycloakId is optional and may be null.
 * initialPassword is used only during Keycloak provisioning and is never persisted.
 */
public record CreateUsuarioCommand(
    String nombre,
    String correo,
    UUID rolId,
    String keycloakId,
    String initialPassword
) {

    public CreateUsuarioCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
        if (rolId == null) {
            throw new IllegalArgumentException("rolId is required");
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalArgumentException("initialPassword is required");
        }
    }
}