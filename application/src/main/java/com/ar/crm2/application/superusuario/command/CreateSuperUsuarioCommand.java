package com.ar.crm2.application.superusuario.command;

import java.util.UUID;

/**
 * Command to create a new SuperUsuario.
 * Required fields validated at construction time.
 */
public record CreateSuperUsuarioCommand(
    String correo,
    String passwordHash
) {

    public CreateSuperUsuarioCommand {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash is required");
        }
    }
}