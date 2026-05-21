package com.ar.crm2.application.empresa.command;

import com.ar.crm2.model.enums.EstadoRelacion;

import java.util.UUID;

/**
 * Command to edit an existing Empresa.
 * Validates id and nombre at construction time.
 */
public record EditEmpresaCommand(
    UUID id,
    String nombre,
    String sector,
    String telefono,
    String paginaWeb,
    String facebook,
    String instagram,
    String twitter,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    String notas
) {

    public EditEmpresaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}