package com.ar.crm2.application.tarea.port.in;

import com.ar.crm2.application.tarea.command.EditTareaCommand;
import com.ar.crm2.model.entity.Tarea;

/**
 * Inbound input port for editing an existing Tarea.
 */
public interface EditTareaUseCase {

    /**
     * Updates an existing Tarea with the given command.
     *
     * @param command holds the id of the Tarea to edit and the updated fields
     * @return the updated domain entity
     */
    Tarea edit(EditTareaCommand command);
}