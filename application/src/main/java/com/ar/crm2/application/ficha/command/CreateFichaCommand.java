package com.ar.crm2.application.ficha.command;

import com.ar.crm2.model.enums.TipoFicha;

import java.util.UUID;

/**
 * Command to create a new Ficha.
 * Required fields validated at construction time.
 * Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 */
public record CreateFichaCommand(
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId,
    UUID responsableId,
    UUID creadoPor
) {

    public CreateFichaCommand {
        if (columnaId == null) {
            throw new IllegalArgumentException("columnaId is required");
        }
        if (tipoFicha == null) {
            throw new IllegalArgumentException("tipoFicha is required");
        }
        if (responsableId == null) {
            throw new IllegalArgumentException("responsableId is required");
        }
        if (creadoPor == null) {
            throw new IllegalArgumentException("creadoPor is required");
        }
    }
}