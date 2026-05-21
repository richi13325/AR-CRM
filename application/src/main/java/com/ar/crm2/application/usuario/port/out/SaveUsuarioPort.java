package com.ar.crm2.application.usuario.port.out;

import com.ar.crm2.model.entity.Usuario;

/**
 * Granular outbound port for persisting a new or updated Usuario.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveUsuarioPort {

    /**
     * Persists a Usuario.
     *
     * @param usuario the domain entity to persist
     * @return the persisted domain entity
     */
    Usuario save(Usuario usuario);
}