package com.ar.crm2.application.tarea.port.in;

import com.ar.crm2.application.tarea.command.DeleteTareaCommand;

/**
 * Inbound input port for deleting an existing Tarea.
 */
public interface DeleteTareaUseCase {

    /**
     * Deletes an existing Tarea by the given command.
     *
     * @param command holds the id of the Tarea to delete
     */
    void delete(DeleteTareaCommand command);
}