package com.ar.crm2.application.superusuario.port.in;

import com.ar.crm2.application.superusuario.command.EditSuperUsuarioCommand;
import com.ar.crm2.model.entity.SuperUsuario;

/**
 * Inbound input port for editing an existing SuperUsuario.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface EditSuperUsuarioUseCase {

    /**
     * Edits an existing SuperUsuario from the given command.
     *
     * @param command holds the id and updated fields
     * @return the updated domain entity
     */
    SuperUsuario edit(EditSuperUsuarioCommand command);
}