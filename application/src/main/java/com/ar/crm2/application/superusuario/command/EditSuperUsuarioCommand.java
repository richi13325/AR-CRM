package com.ar.crm2.application.superusuario.command;

import java.util.UUID;

/**
 * Command to edit an existing SuperUsuario.
 * Validates id and correo at construction time.
 * Does NOT include passwordHash/creadoEn/activo — preserved from the existing entity via SuperUsuario.reconstitute.
 * keycloakId is optional — if present, updates the Keycloak linkage; if null, preserves it.
 */
public record EditSuperUsuarioCommand(
    UUID id,
    String correo,
    String keycloakId
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
