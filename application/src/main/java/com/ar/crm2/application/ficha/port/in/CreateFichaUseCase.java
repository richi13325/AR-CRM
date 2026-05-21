package com.ar.crm2.application.ficha.port.in;

import com.ar.crm2.application.ficha.command.CreateFichaCommand;
import com.ar.crm2.model.entity.Ficha;

/**
 * Inbound input port for creating a new Ficha.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateFichaUseCase {

    /**
     * Creates a new Ficha from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Ficha create(CreateFichaCommand command);
}