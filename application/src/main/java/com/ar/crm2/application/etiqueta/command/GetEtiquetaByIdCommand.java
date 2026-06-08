package com.ar.crm2.application.etiqueta.command;

import java.util.UUID;

/**
 * Command to retrieve a single Etiqueta by id.
 */
public record GetEtiquetaByIdCommand(UUID id) {

    public GetEtiquetaByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}
