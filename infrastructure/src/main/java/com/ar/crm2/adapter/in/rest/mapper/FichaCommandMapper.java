package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateFichaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditFichaRequest;
import com.ar.crm2.application.ficha.command.CreateFichaCommand;
import com.ar.crm2.application.ficha.command.DeleteFichaCommand;
import com.ar.crm2.application.ficha.command.EditFichaCommand;
import com.ar.crm2.application.ficha.command.GetFichaByIdCommand;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class FichaCommandMapper {

    private FichaCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateFichaCommand toCommand(CreateFichaRequest request) {
        return new CreateFichaCommand(
            request.columnaId(),
            request.tipoFicha(),
            request.tratoId(),
            request.tareaId(),
            request.responsableId(),
            request.creadoPor()
        );
    }

    /**
     * Maps an edit request with a query-parameter id to an application command.
     */
    public static EditFichaCommand toCommand(UUID id, EditFichaRequest request) {
        return new EditFichaCommand(
            id,
            request.columnaId(),
            request.tipoFicha(),
            request.tratoId(),
            request.tareaId(),
            request.responsableId()
        );
    }

    /**
     * Maps a query-parameter id to a delete command.
     */
    public static DeleteFichaCommand toDeleteCommand(UUID id) {
        return new DeleteFichaCommand(id);
    }

    /**
     * Maps a query-parameter id to a get-by-id command.
     */
    public static GetFichaByIdCommand toGetByIdCommand(UUID id) {
        return new GetFichaByIdCommand(id);
    }
}