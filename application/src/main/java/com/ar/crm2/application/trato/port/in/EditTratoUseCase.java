package com.ar.crm2.application.trato.port.in;

import com.ar.crm2.application.trato.command.EditTratoCommand;
import com.ar.crm2.model.entity.Trato;

/**
 * Inbound input port for editing an existing Trato.
 */
public interface EditTratoUseCase {

    /**
     * Updates an existing Trato with the given command.
     *
     * @param command holds the id of the Trato to edit and the updated fields
     * @return the updated domain entity
     */
    Trato edit(EditTratoCommand command);
}