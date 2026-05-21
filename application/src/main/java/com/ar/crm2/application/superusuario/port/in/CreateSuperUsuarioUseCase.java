package com.ar.crm2.application.superusuario.port.in;

import com.ar.crm2.application.superusuario.command.CreateSuperUsuarioCommand;
import com.ar.crm2.model.entity.SuperUsuario;

/**
 * Inbound input port for creating a new SuperUsuario.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateSuperUsuarioUseCase {

    /**
     * Creates a new SuperUsuario from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    SuperUsuario create(CreateSuperUsuarioCommand command);
}