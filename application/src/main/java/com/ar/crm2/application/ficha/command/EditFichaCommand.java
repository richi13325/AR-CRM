package com.ar.crm2.application.ficha.command;

import com.ar.crm2.model.enums.TipoFicha;

import java.util.UUID;

/**
 * Command to edit an existing Ficha.
 * Validates id, columnaId, tipoFicha at construction time.
 * Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 */
public record EditFichaCommand(
    UUID id,
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId
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