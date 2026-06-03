package com.ar.crm2.application.contacto.port.in;

import com.ar.crm2.application.contacto.command.CambiarEstadoContactoCommand;
import com.ar.crm2.model.entity.Contacto;

public interface CambiarEstadoContactoUseCase {

    /**
     * Changes the relation state of a Contacto.
     *
     * @param command holds the contactoId and target relation state
     * @return the updated domain entity
     */
    Contacto cambiarEstado(CambiarEstadoContactoCommand command);
}
