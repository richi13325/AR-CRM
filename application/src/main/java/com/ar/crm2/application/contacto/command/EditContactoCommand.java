package com.ar.crm2.application.contacto.command;

import com.ar.crm2.model.enums.EstadoRelacion;

import java.util.UUID;

/**
 * Command to edit an existing Contacto.
 * Validates id and nombre at construction time.
 * Does NOT include creadoPor — that field is preserved from the existing entity.
 */
public record EditContactoCommand(
    UUID id,
    String nombre,
    String correo,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    String telefono,
    String cargo,
    String comoNosConocio
) {

    public EditContactoCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}