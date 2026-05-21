package com.ar.crm2.application.columna.port.in;

import com.ar.crm2.application.columna.command.EditColumnaCommand;
import com.ar.crm2.model.entity.Columna;

/**
 * Inbound input port for editing an existing Columna.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface EditColumnaUseCase {

    /**
     * Updates an existing Columna from the given command.
     *
     * @param command holds the id and updated fields
     * @return the updated domain entity
     */
    Columna edit(EditColumnaCommand command);
}