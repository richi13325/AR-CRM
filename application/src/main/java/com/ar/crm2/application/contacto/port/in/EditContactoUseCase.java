package com.ar.crm2.application.contacto.port.in;

import com.ar.crm2.application.contacto.command.EditContactoCommand;
import com.ar.crm2.model.entity.Contacto;

/**
 * Inbound input port for editing an existing Contacto.
 */
public interface EditContactoUseCase {

    /**
     * Updates an existing Contacto with the given command.
     *
     * @param command holds the id of the Contacto to edit and the updated fields
     * @return the updated domain entity
     */
    Contacto edit(EditContactoCommand command);
}