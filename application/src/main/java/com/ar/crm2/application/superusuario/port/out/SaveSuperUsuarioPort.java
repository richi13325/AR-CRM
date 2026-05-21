package com.ar.crm2.application.superusuario.port.out;

import com.ar.crm2.model.entity.SuperUsuario;

/**
 * Granular outbound port for persisting a new or updated SuperUsuario.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveSuperUsuarioPort {

    /**
     * Persists a SuperUsuario.
     *
     * @param superUsuario the domain entity to persist
     * @return the persisted domain entity
     */
    SuperUsuario save(SuperUsuario superUsuario);
}