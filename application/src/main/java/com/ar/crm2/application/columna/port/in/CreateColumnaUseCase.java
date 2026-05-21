package com.ar.crm2.application.columna.port.in;

import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.model.entity.Columna;

/**
 * Inbound input port for creating a new Columna.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateColumnaUseCase {

    /**
     * Creates a new Columna from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Columna create(CreateColumnaCommand command);
}