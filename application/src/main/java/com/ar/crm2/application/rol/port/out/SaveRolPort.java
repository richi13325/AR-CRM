package com.ar.crm2.application.rol.port.out;

import com.ar.crm2.model.entity.Rol;

/**
 * Granular outbound port for persisting a new or updated Rol.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveRolPort {

    /**
     * Persists a Rol.
     *
     * @param rol the domain entity to persist
     * @return the persisted domain entity
     */
    Rol save(Rol rol);
}