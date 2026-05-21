package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.ReordenarColumnasCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for reordering columns within a Tablero.
 * UseCase suffix per project rules.
 */
public interface ReordenarColumnasUseCase {

    /**
     * Reorders the columns of a Tablero to match the given sequence.
     *
     * @param command holds tableroId and nuevoOrden list
     * @return the updated Tablero aggregate
     */
    Tablero reordenar(ReordenarColumnasCommand command);
}