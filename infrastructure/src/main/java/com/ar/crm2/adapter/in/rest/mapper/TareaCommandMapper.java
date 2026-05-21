package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateTareaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditTareaRequest;
import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.tarea.command.DeleteTareaCommand;
import com.ar.crm2.application.tarea.command.EditTareaCommand;
import com.ar.crm2.application.tarea.command.GetTareaByIdCommand;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class TareaCommandMapper {

    private TareaCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateTareaCommand toCommand(CreateTareaRequest request) {
        return new CreateTareaCommand(
            request.tratoId(),
            request.responsableId(),
            request.titulo(),
            request.descripcion(),
            request.tipo(),
            request.prioridad(),
            request.fechaLimite()
        );
    }

    /**
     * Maps a REST edit request with a path id to an application command.
     */
    public static EditTareaCommand toCommand(UUID id, EditTareaRequest request) {
        return new EditTareaCommand(
            id,
            request.responsableId(),
            request.titulo(),
            request.descripcion(),
            request.tipo(),
            request.prioridad(),
            request.fechaLimite()
        );
    }

    /**
     * Maps a path id to a delete command.
     */
    public static DeleteTareaCommand toDeleteCommand(UUID id) {
        return new DeleteTareaCommand(id);
    }

    /**
     * Maps a path id to a get-by-id command.
     */
    public static GetTareaByIdCommand toGetByIdCommand(UUID id) {
        return new GetTareaByIdCommand(id);
    }
}