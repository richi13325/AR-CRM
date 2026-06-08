package com.ar.crm2.application.ficha.command;

import com.ar.crm2.model.enums.TipoFicha;

import java.util.List;
import java.util.UUID;

/**
 * Command to edit an existing Ficha.
 *
 * <p>Same invariants as {@link CreateFichaCommand}; in addition, the
 * {@code etiquetaIds} list, when present, replaces the existing etiqueta
 * relations in full.
 */
public record EditFichaCommand(
    UUID id,
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId,
    List<UUID> etiquetaIds
) {

    public EditFichaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (columnaId == null) {
            throw new IllegalArgumentException("columnaId is required");
        }
        if (tipoFicha == null) {
            throw new IllegalArgumentException("tipoFicha is required");
        }
    }
}
