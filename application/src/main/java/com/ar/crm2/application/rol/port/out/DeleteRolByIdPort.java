package com.ar.crm2.application.rol.port.out;

import com.ar.crm2.model.vo.RolId;

/**
 * Granular outbound port for deleting a Rol by its id.
 * Single-method contract per project rules.
 */
public interface DeleteRolByIdPort {

    /**
     * Deletes a Rol by its id.
     *
     * @param id the RolId to delete
     */
    void deleteById(RolId id);
}