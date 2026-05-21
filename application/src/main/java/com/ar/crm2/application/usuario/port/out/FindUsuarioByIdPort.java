package com.ar.crm2.application.usuario.port.out;

import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Usuario by its id.
 * Single-method contract per project rules.
 */
public interface FindUsuarioByIdPort {

    /**
     * Finds a Usuario by its id.
     *
     * @param id the UsuarioId to search for
     * @return an Optional containing the Usuario if found, empty otherwise
     */
    Optional<Usuario> findById(UsuarioId id);
}