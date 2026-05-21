package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditColumnaRequest;
import com.ar.crm2.application.columna.command.CreateColumnaCommand;
import com.ar.crm2.application.columna.command.DeleteColumnaCommand;
import com.ar.crm2.application.columna.command.EditColumnaCommand;
import com.ar.crm2.application.columna.command.GetAllColumnasCommand;
import com.ar.crm2.application.columna.command.GetColumnaByIdCommand;

import java.util.Optional;
import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class ColumnaCommandMapper {

    private ColumnaCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateColumnaCommand toCommand(CreateColumnaRequest request) {
        return new CreateColumnaCommand(
            Optional.ofNullable(request.superUsuarioId()),
            request.nombre(),
            request.color(),
            request.tipoTablero(),
            request.tipoColumna()
        );
    }

    /**
     * Maps an edit request with a query-parameter id to an application command.
     */
    public static EditColumnaCommand toCommand(UUID id, EditColumnaRequest request) {
        return new EditColumnaCommand(
            id,
            request.nombre(),
            request.color(),
            request.tipoTablero(),
            request.tipoColumna()
        );
    }

    /**
     * Maps a query-parameter id to a delete command.
     */
    public static DeleteColumnaCommand toDeleteCommand(UUID id) {
        return new DeleteColumnaCommand(id);
    }

    /**
     * Maps a query-parameter id to a get-by-id command.
     */
    public static GetColumnaByIdCommand toGetByIdCommand(UUID id) {
        return new GetColumnaByIdCommand(id);
    }

    /**
     * Maps to an empty get-all command.
     */
    public static GetAllColumnasCommand toGetAllCommand() {
        return new GetAllColumnasCommand();
    }
}