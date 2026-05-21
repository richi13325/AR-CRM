package com.ar.crm2.application.contacto.port.in;

import com.ar.crm2.application.contacto.command.DeleteContactoCommand;

/**
 * Inbound input port for deleting an existing Contacto.
 */
public interface DeleteContactoUseCase {

    /**
     * Deletes an existing Contacto by the given command.
     *
     * @param command holds the id of the Contacto to delete
     */
    void delete(DeleteContactoCommand command);
}