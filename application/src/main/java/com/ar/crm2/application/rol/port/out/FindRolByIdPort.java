package com.ar.crm2.application.rol.port.out;

import com.ar.crm2.model.entity.Rol;
import com.ar.crm2.model.vo.RolId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Rol by its id.
 * Single-method contract per project rules.
 */
public interface FindRolByIdPort {

    /**
     * Finds a Rol by its id.
     *
     * @param id the RolId to search for
     * @return an Optional containing the Rol if found, empty otherwise
     */
    Optional<Rol> findById(RolId id);
}