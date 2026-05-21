package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.DeleteTableroCommand;

/**
 * Inbound input port for deleting an existing Tablero.
 * UseCase suffix per project rules.
 */
public interface DeleteTableroUseCase {

    /**
     * Deletes a Tablero by its id.
     *
     * @param command holds the Tablero id
     */
    void delete(DeleteTableroCommand command);
}