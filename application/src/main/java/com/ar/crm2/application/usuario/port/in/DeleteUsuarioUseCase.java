package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.application.usuario.command.DeleteUsuarioCommand;

/**
 * Inbound input port for deleting a Usuario by its id.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface DeleteUsuarioUseCase {

    /**
     * Deletes a Usuario by its id.
     *
     * @param command holds the id to delete
     */
    void delete(DeleteUsuarioCommand command);
}