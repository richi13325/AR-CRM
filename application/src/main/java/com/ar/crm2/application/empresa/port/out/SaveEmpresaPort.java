package com.ar.crm2.application.empresa.port.out;

import com.ar.crm2.model.entity.Empresa;

/**
 * Granular outbound port for persisting a new Empresa.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveEmpresaPort {

    /**
     * Persists a new Empresa.
     *
     * @param empresa the domain entity to persist
     * @return the persisted domain entity
     */
    Empresa save(Empresa empresa);
}