package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.AsignarColumnaTableroCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for assigning an existing catalog Columna to a Tablero.
 * UseCase suffix per project rules.
 */
public interface AsignarColumnaTableroUseCase {

    /**
     * Assigns an existing catalog Columna to a Tablero with contextual data.
     *
     * @param command holds tableroId, columnaId, and board-specific context (WIP, note, state)
     * @return the updated Tablero aggregate with the new column assignment
     */
    Tablero asignarColumna(AsignarColumnaTableroCommand command);
}