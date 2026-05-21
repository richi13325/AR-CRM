package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.entity.Tablero;

/**
 * Granular outbound port for persisting a new or updated Tablero.
 * Single-method contract per project rules.
 */
public interface SaveTableroPort {

    /**
     * Persists a Tablero (create or update).
     *
     * @param tablero the domain entity to persist
     * @return the persisted domain entity
     */
    Tablero save(Tablero tablero);
}