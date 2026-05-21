package com.ar.crm2.application.contacto.port.out;

import com.ar.crm2.model.entity.Contacto;

/**
 * Granular outbound port for persisting a new or updated Contacto.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveContactoPort {

    /**
     * Persists a Contacto.
     *
     * @param contacto the domain entity to persist
     * @return the persisted domain entity
     */
    Contacto save(Contacto contacto);
}