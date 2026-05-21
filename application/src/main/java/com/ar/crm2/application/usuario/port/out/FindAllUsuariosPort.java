package com.ar.crm2.application.usuario.port.out;

import com.ar.crm2.model.entity.Usuario;

import java.util.List;

/**
 * Granular outbound port for retrieving all Usuarios.
 * Single-method contract per project rules.
 */
public interface FindAllUsuariosPort {

    /**
     * Retrieves all Usuarios.
     *
     * @return list of all Usuario domain entities
     */
    List<Usuario> findAll();
}