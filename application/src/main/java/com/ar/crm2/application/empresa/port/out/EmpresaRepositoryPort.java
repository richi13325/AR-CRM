package com.ar.crm2.application.empresa.port.out;

import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.vo.EmpresaId;

import java.util.List;

/**
 * Outbound port for Empresa persistence.
 * Implementation belongs to infrastructure; this contract lives in application.
 */
public interface EmpresaRepositoryPort {

    /**
     * Persists a new Empresa.
     */
    Empresa save(Empresa empresa);

    /**
     * Retrieves all Empresas.
     * Used for list/get operations.
     */
    List<Empresa> findAll();

    /**
     * Retrieves an Empresa by its identity.
     */
    Empresa findById(EmpresaId id);
}