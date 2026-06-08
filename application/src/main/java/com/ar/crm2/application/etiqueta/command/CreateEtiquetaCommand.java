package com.ar.crm2.application.etiqueta.command;

import com.ar.crm2.model.enums.TipoEtiqueta;

/**
 * Command to create a new Etiqueta in the global catalog.
 * Uniqueness by (nombre, tipoEtiqueta) is enforced at the application boundary.
 */
public record CreateEtiquetaCommand(
    String nombre,
    TipoEtiqueta tipoEtiqueta,
    String color
) {

    public CreateEtiquetaCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (tipoEtiqueta == null) {
            throw new IllegalArgumentException("tipoEtiqueta is required");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("color is required");
        }
    }
}
