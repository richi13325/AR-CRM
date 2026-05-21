package com.ar.crm2.application.rol.port.in;

import com.ar.crm2.model.entity.Rol;

import java.util.List;

/**
 * Inbound input port for retrieving all Rols.
 */
public interface GetAllRolesUseCase {

    /**
     * Retrieves all existing Rols.
     *
     * @return list of all Rols
     */
    List<Rol> getAll();
}