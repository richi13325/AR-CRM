package com.ar.crm2.application.rol.port.in;

import com.ar.crm2.application.rol.command.CreateRolCommand;
import com.ar.crm2.model.entity.Rol;

/**
 * Inbound input port for creating a new Rol.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateRolUseCase {

    /**
     * Creates a new Rol from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Rol create(CreateRolCommand command);
}