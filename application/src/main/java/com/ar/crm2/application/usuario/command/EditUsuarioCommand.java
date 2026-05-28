package com.ar.crm2.application.usuario.command;

import java.util.UUID;

/**
 * Command to edit an existing Usuario.
 * Validates id and fields at construction time.
 * Does NOT include id/rolId/creadoEn/activo — preserved from the existing entity via Usuario.reconstitute.
 * keycloakId is optional — if present, updates the Keycloak linkage; if null, preserves it.
 */
public record EditUsuarioCommand(
    UUID id,
    String nombre,
    String correo,
    UUID rolId,
    String keycloakId
) {

    public EditUsuarioCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo is required");
        }
        if (rolId == null) {
            throw new IllegalArgumentException("rolId is required");
        }
    }
}