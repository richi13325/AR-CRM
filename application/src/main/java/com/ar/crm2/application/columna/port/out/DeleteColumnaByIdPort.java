package com.ar.crm2.application.columna.port.out;

import com.ar.crm2.model.vo.ColumnaId;

/**
 * Granular outbound port for deleting a Columna by its id.
 * Single-method contract per project rules.
 */
public interface DeleteColumnaByIdPort {

    /**
     * Deletes a Columna by its id.
     *
     * @param id the ColumnaId to delete
     */
    void deleteById(ColumnaId id);
}