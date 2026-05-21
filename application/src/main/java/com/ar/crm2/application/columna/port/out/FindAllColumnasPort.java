package com.ar.crm2.application.columna.port.out;

import com.ar.crm2.model.entity.Columna;

import java.util.List;

/**
 * Granular outbound port for retrieving all Columnas.
 * Single-method contract per project rules.
 */
public interface FindAllColumnasPort {

    /**
     * Retrieves all Columnas.
     *
     * @return list of all Columna domain entities
     */
    List<Columna> findAll();
}