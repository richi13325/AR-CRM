package com.ar.crm2.application.superusuario.port.out;

import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;

import java.util.Optional;

/**
 * Granular outbound port for finding a SuperUsuario by its id.
 * Single-method contract per project rules.
 */
public interface FindSuperUsuarioByIdPort {

    /**
     * Finds a SuperUsuario by its id.
     *
     * @param id the SuperUsuarioId to search for
     * @return an Optional containing the SuperUsuario if found, empty otherwise
     */
    Optional<SuperUsuario> findById(SuperUsuarioId id);
}