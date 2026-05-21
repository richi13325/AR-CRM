package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.model.entity.Usuario;

/**
 * Inbound input port for creating a new Usuario.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateUsuarioUseCase {

    /**
     * Creates a new Usuario from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Usuario create(CreateUsuarioCommand command);
}