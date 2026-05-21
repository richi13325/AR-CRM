package com.ar.crm2.application.ficha.port.out;

import com.ar.crm2.model.vo.FichaId;

/**
 * Granular outbound port for deleting a Ficha by its id.
 * Single-method contract per project rules.
 */
public interface DeleteFichaByIdPort {

    /**
     * Deletes a Ficha by its id.
     *
     * @param id the FichaId to delete
     */
    void deleteById(FichaId id);
}