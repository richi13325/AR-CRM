package com.ar.crm2.application.contacto.command;

import com.ar.crm2.model.enums.EstadoRelacion;

import java.util.UUID;

public record CambiarEstadoContactoCommand(
    UUID contactoId,
    EstadoRelacion nuevoEstado
) {

    public CambiarEstadoContactoCommand {
        if (contactoId == null) {
            throw new IllegalArgumentException("contactoId is required");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("nuevoEstado is required");
        }
    }
}
