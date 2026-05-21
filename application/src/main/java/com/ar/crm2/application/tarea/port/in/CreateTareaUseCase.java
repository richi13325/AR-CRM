package com.ar.crm2.application.tarea.port.in;

import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.model.entity.Tarea;

/**
 * Inbound input port for creating a new Tarea.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateTareaUseCase {

    /**
     * Creates a new Tarea from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Tarea create(CreateTareaCommand command);
}