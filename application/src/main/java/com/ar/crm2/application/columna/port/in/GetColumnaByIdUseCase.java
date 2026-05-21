package com.ar.crm2.application.columna.port.in;

import com.ar.crm2.application.columna.command.GetColumnaByIdCommand;
import com.ar.crm2.model.entity.Columna;

/**
 * Inbound input port for retrieving a Columna by its id.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface GetColumnaByIdUseCase {

    /**
     * Retrieves a Columna by its id.
     *
     * @param command holds the ColumnaId
     * @return the Columna domain entity
     */
    Columna getById(GetColumnaByIdCommand command);
}