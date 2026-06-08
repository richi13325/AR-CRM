package com.ar.crm2.application.ficha.command;

import com.ar.crm2.model.enums.TipoFicha;

import java.util.List;
import java.util.UUID;

/**
 * Command to create a new Ficha (Kanban card).
 *
 * <p>Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 *
 * <p>The {@code etiquetaIds} list is optional (may be null/empty) and is resolved
 * against the global Etiqueta catalog at the application boundary. Resolved Etiquetas
 * must match the Ficha's tipoFicha.
 */
public record CreateFichaCommand(
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId,
    List<UUID> etiquetaIds
) {

    public CreateFichaCommand {
        if (columnaId == null) {
            throw new IllegalArgumentException("columnaId is required");
        }
        if (tipoFicha == null) {
            throw new IllegalArgumentException("tipoFicha is required");
        }
    }
}
