package com.ar.crm2.application.trato.port.out;

import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.vo.TratoId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Trato by its id.
 * Single-method contract per project rules.
 */
public interface FindTratoByIdPort {

    /**
     * Finds a Trato by its id.
     *
     * @param id the TratoId to search for
     * @return an Optional containing the Trato if found, empty otherwise
     */
    Optional<Trato> findById(TratoId id);
}