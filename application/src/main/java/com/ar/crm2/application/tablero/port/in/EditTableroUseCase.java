package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.EditTableroCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for editing an existing Tablero.
 * UseCase suffix per project rules.
 */
public interface EditTableroUseCase {

    /**
     * Edits an existing Tablero with new name and description.
     *
     * @param command holds the Tablero id and new fields
     * @return the updated domain entity
     */
    Tablero edit(EditTableroCommand command);
}