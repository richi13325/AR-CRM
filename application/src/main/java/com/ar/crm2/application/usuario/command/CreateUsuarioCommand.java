package com.ar.crm2.application.usuario.command;

import java.util.UUID;

/**
 * Command to create a new Usuario.
 * Required fields validated at construction time.
 * keycloakId is optional and may be null.
 */
public record CreateUsuarioCommand(
    String nombre,
    String correo,
    String passwordHash,
    UUID rolId,
    String keycloakId
) {

    public CreateUsuarioCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash is required");
        }
        if (rolId == null) {
            throw new IllegalArgumentException("rolId is required");
        }
    }
}