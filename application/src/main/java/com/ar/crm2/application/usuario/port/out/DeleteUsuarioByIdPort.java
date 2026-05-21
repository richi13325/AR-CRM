package com.ar.crm2.application.usuario.port.out;

import com.ar.crm2.model.vo.UsuarioId;

/**
 * Granular outbound port for deleting a Usuario by its id.
 * Single-method contract per project rules.
 */
public interface DeleteUsuarioByIdPort {

    /**
     * Deletes a Usuario by its id.
     *
     * @param id the UsuarioId to delete
     */
    void deleteById(UsuarioId id);
}