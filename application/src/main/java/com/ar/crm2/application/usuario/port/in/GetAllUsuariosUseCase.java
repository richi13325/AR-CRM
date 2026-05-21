package com.ar.crm2.application.usuario.port.in;

import com.ar.crm2.model.entity.Usuario;

import java.util.List;

/**
 * Inbound input port for retrieving all Usuarios.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface GetAllUsuariosUseCase {

    /**
     * Retrieves all Usuarios.
     *
     * @return list of all Usuario domain entities
     */
    List<Usuario> getAll();
}