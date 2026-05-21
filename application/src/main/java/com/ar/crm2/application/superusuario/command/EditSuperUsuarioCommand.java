package com.ar.crm2.application.superusuario.command;

import java.util.UUID;

/**
 * Command to edit an existing SuperUsuario.
 * Validates id and correo at construction time.
 * Does NOT include passwordHash/creadoEn/activo — preserved from the existing entity via SuperUsuario.reconstitute.
 */
public record EditSuperUsuarioCommand(
    UUID id,
    String correo
) {

    public EditSuperUsuarioCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
    }
}