package com.ar.crm2.application.etiqueta.port.in;

import com.ar.crm2.application.etiqueta.command.DeleteEtiquetaCommand;

/**
 * Inbound input port for deleting an Etiqueta.
 * When the Etiqueta is in use, the {@code confirm} flag in the command
 * must be true or the call is rejected.
 */
public interface DeleteEtiquetaUseCase {

    void delete(DeleteEtiquetaCommand command);
}
