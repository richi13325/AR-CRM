package com.ar.crm2.application.contacto.port.in;

import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.model.entity.Contacto;

/**
 * Inbound input port for creating a new Contacto.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateContactoUseCase {

    /**
     * Creates a new Contacto from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Contacto create(CreateContactoCommand command);
}