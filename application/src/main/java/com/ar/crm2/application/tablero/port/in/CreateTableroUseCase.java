package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for creating a new Tablero.
 * UseCase suffix per project rules.
 */
public interface CreateTableroUseCase {

    /**
     * Creates a new Tablero from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Tablero create(CreateTableroCommand command);
}