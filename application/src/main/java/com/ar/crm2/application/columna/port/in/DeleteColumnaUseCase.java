package com.ar.crm2.application.columna.port.in;

import com.ar.crm2.application.columna.command.DeleteColumnaCommand;

/**
 * Inbound input port for deleting a Columna.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface DeleteColumnaUseCase {

    /**
     * Deletes a Columna by its id.
     * Guard: fails if the Columna has associated Fichas.
     *
     * @param command holds the ColumnaId
     */
    void delete(DeleteColumnaCommand command);
}