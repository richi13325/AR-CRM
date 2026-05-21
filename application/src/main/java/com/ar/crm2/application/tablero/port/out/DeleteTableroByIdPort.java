package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.vo.TableroId;

/**
 * Granular outbound port for deleting a Tablero by its id.
 * Single-method contract per project rules.
 */
public interface DeleteTableroByIdPort {

    /**
     * Deletes a Tablero by its id.
     *
     * @param id the TableroId to delete
     */
    void deleteById(TableroId id);
}