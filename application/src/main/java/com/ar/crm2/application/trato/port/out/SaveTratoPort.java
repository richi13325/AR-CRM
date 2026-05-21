package com.ar.crm2.application.trato.port.out;

import com.ar.crm2.model.entity.Trato;

/**
 * Granular outbound port for persisting a new or updated Trato.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveTratoPort {

    /**
     * Persists a Trato.
     *
     * @param trato the domain entity to persist
     * @return the persisted domain entity
     */
    Trato save(Trato trato);
}