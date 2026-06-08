package com.ar.crm2.application.etiqueta.command;

import com.ar.crm2.model.enums.TipoEtiqueta;

/**
 * Command to list Etiquetas in the global catalog, optionally filtered by type.
 */
public record GetAllEtiquetasCommand(
    TipoEtiqueta tipoEtiqueta
) {
    public GetAllEtiquetasCommand {
        // tipoEtiqueta may be null (no filter)
    }
}
