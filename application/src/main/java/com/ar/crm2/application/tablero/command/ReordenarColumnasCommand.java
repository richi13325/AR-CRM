package com.ar.crm2.application.tablero.command;

import com.ar.crm2.model.vo.ColumnaId;

import java.util.List;
import java.util.UUID;

/**
 * Command to reorder columns within an existing Tablero.
 *
 * <p>The nuevoOrden list must contain exactly the same ColumnaId values
 * currently present in the Tablero, with no duplicates.
 */
public record ReordenarColumnasCommand(
    UUID tableroId,
    List<ColumnaId> nuevoOrden
) {

    public ReordenarColumnasCommand {
        if (tableroId == null) {
            throw new IllegalArgumentException("tableroId is required");
        }
        if (nuevoOrden == null) {
            throw new IllegalArgumentException("nuevoOrden is required");
        }
    }
}