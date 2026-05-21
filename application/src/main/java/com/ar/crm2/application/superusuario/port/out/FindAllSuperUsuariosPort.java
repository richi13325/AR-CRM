package com.ar.crm2.application.superusuario.port.out;

import com.ar.crm2.model.entity.SuperUsuario;

import java.util.List;

/**
 * Granular outbound port for retrieving all SuperUsuarios.
 * Single-method contract per project rules.
 */
public interface FindAllSuperUsuariosPort {

    /**
     * Retrieves all SuperUsuarios.
     *
     * @return list of all SuperUsuario domain entities
     */
    List<SuperUsuario> findAll();
}