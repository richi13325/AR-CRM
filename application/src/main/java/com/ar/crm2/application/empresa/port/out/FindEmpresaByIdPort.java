package com.ar.crm2.application.empresa.port.out;

import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.vo.EmpresaId;

import java.util.Optional;

/**
 * Granular outbound port for finding an Empresa by its id.
 * Single-method contract per project rules.
 */
public interface FindEmpresaByIdPort {

    /**
     * Finds an Empresa by its id.
     *
     * @param id the EmpresaId to search for
     * @return an Optional containing the Empresa if found, empty otherwise
     */
    Optional<Empresa> findById(EmpresaId id);
}