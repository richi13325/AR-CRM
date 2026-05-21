package com.ar.crm2.application.trato.port.in;

import com.ar.crm2.application.trato.command.DeleteTratoCommand;

/**
 * Inbound input port for deleting an existing Trato.
 */
public interface DeleteTratoUseCase {

    /**
     * Deletes an existing Trato by the given command.
     *
     * @param command holds the id of the Trato to delete
     */
    void delete(DeleteTratoCommand command);
}