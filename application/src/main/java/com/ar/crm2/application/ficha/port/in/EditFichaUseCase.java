package com.ar.crm2.application.ficha.port.in;

import com.ar.crm2.application.ficha.command.EditFichaCommand;
import com.ar.crm2.model.entity.Ficha;

/**
 * Inbound input port for editing an existing Ficha.
 */
public interface EditFichaUseCase {

    /**
     * Updates an existing Ficha with the given command.
     *
     * @param command holds the id of the Ficha to edit and the updated fields
     * @return the updated domain entity
     */
    Ficha edit(EditFichaCommand command);
}