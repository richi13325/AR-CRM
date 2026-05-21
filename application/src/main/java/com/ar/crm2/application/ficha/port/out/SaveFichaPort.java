package com.ar.crm2.application.ficha.port.out;

import com.ar.crm2.model.entity.Ficha;

/**
 * Granular outbound port for persisting a new or updated Ficha.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveFichaPort {

    /**
     * Persists a Ficha.
     *
     * @param ficha the domain entity to persist
     * @return the persisted domain entity
     */
    Ficha save(Ficha ficha);
}