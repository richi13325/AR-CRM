package com.ar.crm2.application.tarea.port.out;

import com.ar.crm2.model.vo.TareaId;

/**
 * Granular outbound port for deleting a Tarea by its id.
 * Single-method contract per project rules.
 */
public interface DeleteTareaByIdPort {

    /**
     * Deletes a Tarea by its id.
     *
     * @param id the TareaId to delete
     */
    void deleteById(TareaId id);
}