package com.ar.crm2.application.columna.port.out;

import com.ar.crm2.model.entity.Columna;

/**
 * Granular outbound port for persisting a new or updated Columna.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveColumnaPort {

    /**
     * Persists a Columna.
     *
     * @param columna the domain entity to persist
     * @return the persisted domain entity
     */
    Columna save(Columna columna);
}