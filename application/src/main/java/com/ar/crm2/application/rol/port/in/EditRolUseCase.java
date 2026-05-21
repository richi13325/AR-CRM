package com.ar.crm2.application.rol.port.in;

import com.ar.crm2.application.rol.command.EditRolCommand;
import com.ar.crm2.model.entity.Rol;

/**
 * Inbound input port for editing an existing Rol.
 */
public interface EditRolUseCase {

    /**
     * Updates an existing Rol from the given command.
     *
     * @param command holds the id and updated fields
     * @return the updated domain entity
     */
    Rol edit(EditRolCommand command);
}