package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.GetTableroByIdCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for retrieving a Tablero by its id.
 * UseCase suffix per project rules.
 */
public interface GetTableroByIdUseCase {

    /**
     * Finds a Tablero by its id.
     *
     * @param command holds the Tablero id
     * @return the Tablero domain entity
     */
    Tablero getById(GetTableroByIdCommand command);
}