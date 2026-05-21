package com.ar.crm2.application.tarea.port.out;

import com.ar.crm2.model.entity.Tarea;

/**
 * Granular outbound port for persisting a new or updated Tarea.
 * Single-method contract per project rules.
 * Implementation belongs to infrastructure; contract lives in application.
 */
public interface SaveTareaPort {

    /**
     * Persists a Tarea.
     *
     * @param tarea the domain entity to persist
     * @return the persisted domain entity
     */
    Tarea save(Tarea tarea);
}