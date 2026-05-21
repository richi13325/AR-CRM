package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.application.usuario.command.GetUsuarioByIdCommand;
import com.ar.crm2.model.entity.Usuario;

/**
 * Inbound input port for retrieving a Usuario by its id.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface GetUsuarioByIdUseCase {

    /**
     * Retrieves a Usuario by its id.
     *
     * @param command holds the id to search for
     * @return the found domain entity
     */
    Usuario getById(GetUsuarioByIdCommand command);
}