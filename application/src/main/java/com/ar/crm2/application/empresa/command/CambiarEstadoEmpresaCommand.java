package com.ar.crm2.application.empresa.command;

import com.ar.crm2.model.enums.EstadoRelacion;

import java.util.UUID;

public record CambiarEstadoEmpresaCommand(
    UUID empresaId,
    EstadoRelacion nuevoEstado
) {

    public CambiarEstadoEmpresaCommand {
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("nuevoEstado is required");
        }
    }
}
