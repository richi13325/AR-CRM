package com.ar.crm2.application.contacto.port.in;

import com.ar.crm2.application.contacto.command.GetContactoByIdCommand;
import com.ar.crm2.model.entity.Contacto;

/**
 * Inbound input port for retrieving a specific Contacto by id.
 */
public interface GetContactoByIdUseCase {

    /**
     * Retrieves a Contacto by the given command.
     *
     * @param command holds the id of the Contacto to retrieve
     * @return the requested Contacto domain entity
     */
    Contacto getById(GetContactoByIdCommand command);
}