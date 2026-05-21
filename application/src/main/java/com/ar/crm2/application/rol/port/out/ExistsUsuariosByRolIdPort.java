package com.ar.crm2.application.rol.port.out;

import com.ar.crm2.model.vo.RolId;

/**
 * Granular outbound port for checking if a Rol has associated Usuarios.
 * Single-method contract per project rules.
 * Used as delete guard to prevent removal of Rols assigned to Usuarios.
 */
public interface ExistsUsuariosByRolIdPort {

    /**
     * Checks whether any Usuarios are associated with the given Rol.
     *
     * @param id the RolId to check
     * @return true if Usuarios exist for this Rol, false otherwise
     */
    boolean existsUsuariosByRolId(RolId id);
}