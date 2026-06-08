package com.ar.crm2.application.etiqueta.port.in;

import com.ar.crm2.application.etiqueta.command.GetEtiquetaByIdCommand;
import com.ar.crm2.model.entity.Etiqueta;

/**
 * Inbound input port for retrieving a single Etiqueta by id.
 */
public interface GetEtiquetaByIdUseCase {

    Etiqueta getById(GetEtiquetaByIdCommand command);
}
