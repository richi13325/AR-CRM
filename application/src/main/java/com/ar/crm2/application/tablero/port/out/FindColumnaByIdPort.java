package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.vo.ColumnaId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Columna by its id.
 * Single-method contract per project rules.
 *
 * <p>Note: The canonical {@link FindColumnaByIdPort} lives in the columna package.
 * This interface exists to keep tablero's inbound ports self-contained.
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