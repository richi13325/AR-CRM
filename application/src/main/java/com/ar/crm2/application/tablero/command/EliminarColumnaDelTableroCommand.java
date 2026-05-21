package com.ar.crm2.application.tablero.command;

import java.util.UUID;

/**
 * Command to remove a column from an existing Tablero.
 *
 * <p>The application layer queries {@link com.ar.crm2.application.tablero.port.out.ExistsFichasByColumnaIdPort}
 * before calling the domain behavior, passing the result as {@code tieneFichas} to
 * {@link com.ar.crm2.model.entity.Tablero#eliminarColumnaDelTablero}.
 */
public record EliminarColumnaDelTableroCommand(
    UUID tableroId,
    UUID columnaId
) {

    public EliminarColumnaDelTableroCommand {
        if (tableroId == null) {
            throw new IllegalArgumentException("tableroId is required");
        }
        if (columnaId == null) {
            throw new IllegalArgumentException("columnaId is required");
        }
    }
}