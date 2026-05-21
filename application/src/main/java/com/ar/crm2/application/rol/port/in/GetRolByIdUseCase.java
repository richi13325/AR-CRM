package com.ar.crm2.application.rol.port.in;

import com.ar.crm2.application.rol.command.GetRolByIdCommand;
import com.ar.crm2.model.entity.Rol;

/**
 * Inbound input port for retrieving a Rol by its id.
 */
public interface GetRolByIdUseCase {

    /**
     * Retrieves a Rol by its id.
     *
     * @param command holds the id to search for
     * @return the Rol if found
     * @throws com.ar.crm2.application.rol.exception.RolNotFoundException if not found
     */
    Rol getById(GetRolByIdCommand command);
}