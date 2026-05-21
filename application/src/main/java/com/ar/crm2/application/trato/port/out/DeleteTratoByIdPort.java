package com.ar.crm2.application.trato.port.out;

import com.ar.crm2.model.vo.TratoId;

/**
 * Granular outbound port for deleting a Trato by its id.
 * Single-method contract per project rules.
 */
public interface DeleteTratoByIdPort {

    /**
     * Deletes a Trato by its id.
     *
     * @param id the TratoId to delete
     */
    void deleteById(TratoId id);
}