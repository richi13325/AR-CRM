package com.ar.crm2.application.superusuario.port.in;

import com.ar.crm2.application.superusuario.command.DeleteSuperUsuarioCommand;

/**
 * Inbound input port for deleting a SuperUsuario by its id.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface DeleteSuperUsuarioUseCase {

    /**
     * Deletes a SuperUsuario by its id.
     *
     * @param command holds the id to delete
     */
    void delete(DeleteSuperUsuarioCommand command);
}