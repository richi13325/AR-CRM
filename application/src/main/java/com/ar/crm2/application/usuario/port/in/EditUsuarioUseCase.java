package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.application.usuario.command.EditUsuarioCommand;
import com.ar.crm2.model.entity.Usuario;

/**
 * Inbound input port for editing an existing Usuario.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface EditUsuarioUseCase {

    /**
     * Edits an existing Usuario from the given command.
     *
     * @param command holds the id and updated fields
     * @return the updated domain entity
     */
    Usuario edit(EditUsuarioCommand command);
}