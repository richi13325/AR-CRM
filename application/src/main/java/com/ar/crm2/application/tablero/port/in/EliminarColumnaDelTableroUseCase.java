package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.EliminarColumnaDelTableroCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for removing a column from an existing Tablero.
 * UseCase suffix per project rules.
 *
 * <p>Guard: before calling domain behavior, the service queries
 * {@link com.ar.crm2.application.ficha.port.out.ExistsFichasByColumnaIdPort}
 * to determine if the column contains fichas.
 */
public interface EliminarColumnaDelTableroUseCase {

    /**
     * Removes a column from a Tablero, failing if the column contains fichas.
     *
     * @param command holds tableroId and columnaId
     * @return the updated Tablero aggregate
     */
    Tablero eliminarColumna(EliminarColumnaDelTableroCommand command);
}