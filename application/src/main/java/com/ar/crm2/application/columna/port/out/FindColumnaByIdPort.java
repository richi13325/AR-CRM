package com.ar.crm2.application.columna.port.out;

import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.vo.ColumnaId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Columna by its id.
 * Single-method contract per project rules.
 */
public interface FindColumnaByIdPort {

    /**
     * Finds a Columna by its id.
     *
     * @param id the ColumnaId to search for
     * @return an Optional containing the Columna if found, empty otherwise
     */
    Optional<Columna> findById(ColumnaId id);
}