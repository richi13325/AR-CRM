package com.ar.crm2.application.superusuario.port.in;

import com.ar.crm2.application.superusuario.command.GetSuperUsuarioByIdCommand;
import com.ar.crm2.model.entity.SuperUsuario;

/**
 * Inbound input port for retrieving a SuperUsuario by its id.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface GetSuperUsuarioByIdUseCase {

    /**
     * Retrieves a SuperUsuario by its id.
     *
     * @param command holds the id to search for
     * @return the found domain entity
     */
    SuperUsuario getById(GetSuperUsuarioByIdCommand command);
}