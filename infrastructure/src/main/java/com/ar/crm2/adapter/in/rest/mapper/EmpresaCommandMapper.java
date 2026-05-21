package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEmpresaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditEmpresaRequest;
import com.ar.crm2.application.empresa.command.CreateEmpresaCommand;
import com.ar.crm2.application.empresa.command.DeleteEmpresaCommand;
import com.ar.crm2.application.empresa.command.EditEmpresaCommand;

import java.util.UUID;

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

    /**
     * Maps a REST edit request with a path id to an application command.
     */
    public static EditEmpresaCommand toCommand(UUID id, EditEmpresaRequest request) {
        return new EditEmpresaCommand(
            id,
            request.nombre(),
            request.sector(),
            request.telefono(),
            request.paginaWeb(),
            request.facebook(),
            request.instagram(),
            request.twitter(),
            request.estadoRelacion(),
            request.responsableId(),
            request.notas()
        );
    }

    /**
     * Maps a path id to a delete command.
     */
    public static DeleteEmpresaCommand toDeleteCommand(UUID id) {
        return new DeleteEmpresaCommand(id);
    }
}