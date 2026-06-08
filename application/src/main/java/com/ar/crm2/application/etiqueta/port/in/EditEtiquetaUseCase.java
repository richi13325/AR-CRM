package com.ar.crm2.application.etiqueta.port.in;

import com.ar.crm2.application.etiqueta.command.EditEtiquetaCommand;
import com.ar.crm2.model.entity.Etiqueta;

/**
 * Inbound input port for editing an existing Etiqueta.
 */
public interface EditEtiquetaUseCase {

    /**
     * Updates the name and color of an existing Etiqueta.
     *
     * @param command the edit command
     * @return the updated Etiqueta
     */
    Etiqueta edit(EditEtiquetaCommand command);
}
