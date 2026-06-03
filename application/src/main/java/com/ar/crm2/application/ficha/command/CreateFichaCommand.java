package com.ar.crm2.application.ficha.command;

import com.ar.crm2.model.enums.TipoFicha;

import java.util.UUID;

/**
 * Command to create a new Ficha (Kanban card).
 * Required fields validated at construction time.
 * Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 */
public record CreateFichaCommand(
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId
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