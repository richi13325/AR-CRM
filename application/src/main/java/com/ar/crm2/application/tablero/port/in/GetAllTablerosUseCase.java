package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.model.entity.Tablero;

import java.util.List;

/**
 * Inbound input port for retrieving all Tableros.
 * UseCase suffix per project rules.
 */
public interface GetAllTablerosUseCase {

    /**
     * Returns all persisted Tableros.
     *
     * @return list of all Tablero entities
     */
    List<Tablero> getAll();
}