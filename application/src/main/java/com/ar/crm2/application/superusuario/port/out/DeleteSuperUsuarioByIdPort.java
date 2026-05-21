package com.ar.crm2.application.superusuario.port.out;

import com.ar.crm2.model.vo.SuperUsuarioId;

/**
 * Granular outbound port for deleting a SuperUsuario by its id.
 * Single-method contract per project rules.
 */
public interface DeleteSuperUsuarioByIdPort {

    /**
     * Deletes a SuperUsuario by its id.
     *
     * @param id the SuperUsuarioId to delete
     */
    void deleteById(SuperUsuarioId id);
}