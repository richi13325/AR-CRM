package com.ar.crm2.application.rol.port.out;

import com.ar.crm2.model.entity.Rol;

import java.util.List;

/**
 * Granular outbound port for retrieving all Rols.
 * Single-method contract per project rules.
 */
public interface FindAllRolesPort {

    /**
     * Retrieves all Rols.
     *
     * @return list of all Rol domain entities
     */
    List<Rol> findAll();
}