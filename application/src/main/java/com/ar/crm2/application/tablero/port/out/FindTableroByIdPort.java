package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.vo.TableroId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Tablero by its id.
 * Single-method contract per project rules.
 */
public interface FindTableroByIdPort {

    /**
     * Finds a Tablero by its id.
     *
     * @param id the TableroId to search for
     * @return an Optional containing the Tablero if found, empty otherwise
     */
    Optional<Tablero> findById(TableroId id);
}