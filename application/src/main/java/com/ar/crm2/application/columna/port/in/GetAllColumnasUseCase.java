package com.ar.crm2.application.columna.port.in;

import com.ar.crm2.model.entity.Columna;

import java.util.List;

/**
 * Inbound input port for retrieving all Columnas.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface GetAllColumnasUseCase {

    /**
     * Retrieves all Columnas.
     *
     * @return list of all Columna domain entities
     */
    List<Columna> getAll();
}