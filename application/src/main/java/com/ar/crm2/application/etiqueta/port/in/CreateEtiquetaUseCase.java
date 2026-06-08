package com.ar.crm2.application.etiqueta.port.in;

import com.ar.crm2.application.etiqueta.command.CreateEtiquetaCommand;
import com.ar.crm2.model.entity.Etiqueta;

/**
 * Inbound input port for creating a new Etiqueta in the global catalog.
 */
public interface CreateEtiquetaUseCase {

    /**
     * Creates a new Etiqueta after validating uniqueness of (nombre, tipoEtiqueta).
     *
     * @param command the create command
     * @return the created Etiqueta
     */
    Etiqueta create(CreateEtiquetaCommand command);
}
