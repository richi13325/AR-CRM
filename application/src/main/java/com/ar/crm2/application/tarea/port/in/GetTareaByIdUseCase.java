package com.ar.crm2.application.tarea.port.in;

import com.ar.crm2.application.tarea.command.GetTareaByIdCommand;
import com.ar.crm2.model.entity.Tarea;

/**
 * Inbound input port for retrieving a specific Tarea by id.
 */
public interface GetTareaByIdUseCase {

    /**
     * Retrieves a Tarea by the given command.
     *
     * @param command holds the id of the Tarea to retrieve
     * @return the requested Tarea domain entity
     */
    Tarea getById(GetTareaByIdCommand command);
}