package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateFichaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditFichaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.MoverColumnaRequest;
import com.ar.crm2.application.ficha.command.CreateFichaCommand;
import com.ar.crm2.application.ficha.command.DeleteFichaCommand;
import com.ar.crm2.application.ficha.command.EditFichaCommand;
import com.ar.crm2.application.ficha.command.GetFichaByIdCommand;
import com.ar.crm2.application.ficha.command.MoverColumnaFichaCommand;
import com.ar.crm2.application.security.ActorContext;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class FichaCommandMapper {

    private FichaCommandMapper() {}

    /**
     * Maps a REST create request with actor context to an application command.
     * Note: responsableId/creadoPor now belong to Tarea/Trato, not Ficha.
     */
    public static CreateFichaCommand toCommand(CreateFichaRequest request, ActorContext actorContext) {
        return new CreateFichaCommand(
            request.columnaId(),
            request.tipoFicha(),
            request.tratoId(),
            request.tareaId()
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
            request.tareaId()
        );
    }

    /**
     * Maps a REST request to a mover columna command.
     */
    public static MoverColumnaFichaCommand toMoverColumnaCommand(UUID id, MoverColumnaRequest request) {
        return new MoverColumnaFichaCommand(id, request.targetColumnaId());
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