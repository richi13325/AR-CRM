package com.ar.crm2.application.trato.port.in;

import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.model.entity.Trato;

/**
 * Inbound input port for creating a new Trato.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateTratoUseCase {

    /**
     * Creates a new Trato from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Trato create(CreateTratoCommand command);
}