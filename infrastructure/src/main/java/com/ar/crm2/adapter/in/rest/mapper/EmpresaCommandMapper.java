package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEmpresaRequest;
import com.ar.crm2.application.empresa.command.CreateEmpresaCommand;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class EmpresaCommandMapper {

    private EmpresaCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateEmpresaCommand toCommand(CreateEmpresaRequest request) {
        return new CreateEmpresaCommand(
            request.nombre(),
            request.sector(),
            request.telefono(),
            request.paginaWeb(),
            request.facebook(),
            request.instagram(),
            request.twitter(),
            request.estadoRelacion(),
            request.responsableId(),
            request.creadoPor(),
            request.notas()
        );
    }
}