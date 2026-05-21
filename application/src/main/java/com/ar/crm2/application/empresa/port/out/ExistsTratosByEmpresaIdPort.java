package com.ar.crm2.application.empresa.port.out;

import com.ar.crm2.model.vo.EmpresaId;

/**
 * Granular outbound port for checking if an Empresa has associated Tratos.
 * Single-method contract per project rules.
 */
public interface ExistsTratosByEmpresaIdPort {

    /**
     * Checks whether any Tratos are associated with the given Empresa.
     *
     * @param empresaId the EmpresaId to check
     * @return true if at least one Trato exists for the Empresa, false otherwise
     */
    boolean existsTratosByEmpresaId(EmpresaId empresaId);
}