package com.ar.crm2.application.empresa.command;

import com.ar.crm2.model.enums.EstadoRelacion;

import java.util.UUID;

/**
 * Command to create a new Empresa.
 * Only nombre is required — domain handles validation via Empresa.create(nombre, ...).
 * Optional fields default to null.
 */
public record CreateEmpresaCommand(
    String nombre,
    String sector,
    String telefono,
    String paginaWeb,
    String facebook,
    String instagram,
    String twitter,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    UUID creadoPor,
    String notas
) {

    public CreateEmpresaCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}
