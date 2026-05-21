package com.ar.crm2.application.superusuario.port.in;

import com.ar.crm2.model.entity.SuperUsuario;

import java.util.List;

/**
 * Inbound input port for retrieving all SuperUsuarios.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface GetAllSuperUsuariosUseCase {

    /**
     * Retrieves all SuperUsuarios.
     *
     * @return list of all SuperUsuario domain entities
     */
    List<SuperUsuario> getAll();
}