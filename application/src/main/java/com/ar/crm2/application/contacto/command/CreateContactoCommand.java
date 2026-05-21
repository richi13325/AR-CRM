package com.ar.crm2.application.contacto.command;

import com.ar.crm2.model.enums.EstadoRelacion;

import java.util.UUID;

/**
 * Command to create a new Contacto.
 * Required fields validated at construction time.
 * Optional fields default to null.
 */
public record CreateContactoCommand(
    UUID empresaId,
    String nombre,
    String correo,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    UUID creadoPor,
    String telefono,
    String cargo,
    String comoNosConocio
) {

    public CreateContactoCommand {
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}