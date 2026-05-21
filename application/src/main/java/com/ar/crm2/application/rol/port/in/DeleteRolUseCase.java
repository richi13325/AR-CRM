package com.ar.crm2.application.rol.port.in;

import com.ar.crm2.application.rol.command.DeleteRolCommand;

/**
 * Inbound input port for deleting a Rol.
 */
public interface DeleteRolUseCase {

    /**
     * Deletes a Rol by its id.
     *
     * @param command holds the id to delete
     */
    void delete(DeleteRolCommand command);
}