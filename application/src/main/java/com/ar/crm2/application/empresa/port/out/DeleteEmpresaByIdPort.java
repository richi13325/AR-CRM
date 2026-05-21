package com.ar.crm2.application.empresa.port.out;

import com.ar.crm2.model.vo.EmpresaId;

/**
 * Granular outbound port for hard-deleting an Empresa by its id.
 * Single-method contract per project rules.
 */
public interface DeleteEmpresaByIdPort {

    /**
     * Hard-deletes an Empresa by its id.
     *
     * @param id the EmpresaId to delete
     */
    void deleteById(EmpresaId id);
}