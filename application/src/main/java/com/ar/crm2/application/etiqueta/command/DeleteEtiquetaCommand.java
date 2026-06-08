package com.ar.crm2.application.etiqueta.command;

import java.util.UUID;

/**
 * Command to delete an Etiqueta.
 * When the Etiqueta is in use (FichaEtiqueta relations exist), the
 * {@code confirm} flag must be true or the call is rejected.
 */
public record DeleteEtiquetaCommand(
    UUID id,
    boolean confirm
) {

    public DeleteEtiquetaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}
