package com.ar.crm2.application.ficha.port.in;

import com.ar.crm2.application.ficha.command.DeleteFichaCommand;

/**
 * Inbound input port for deleting an existing Ficha.
 */
public interface DeleteFichaUseCase {

    /**
     * Deletes an existing Ficha by the given command.
     *
     * @param command holds the id of the Ficha to delete
     */
    void delete(DeleteFichaCommand command);
}