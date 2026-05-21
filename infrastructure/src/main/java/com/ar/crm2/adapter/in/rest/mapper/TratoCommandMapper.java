package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateTratoRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditTratoRequest;
import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.application.trato.command.DeleteTratoCommand;
import com.ar.crm2.application.trato.command.EditTratoCommand;
import com.ar.crm2.application.trato.command.GetTratoByIdCommand;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class TratoCommandMapper {

    private TratoCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateTratoCommand toCommand(CreateTratoRequest request) {
        return new CreateTratoCommand(
            request.contactoId(),
            request.responsableId(),
            request.nombre(),
            request.valorEstimado(),
            request.probabilidad(),
            request.fechaCierreEsperada(),
            request.tipoContrato()
        );
    }

    /**
     * Maps a REST edit request with a path id to an application command.
     */
    public static EditTratoCommand toCommand(UUID id, EditTratoRequest request) {
        return new EditTratoCommand(
            id,
            request.responsableId(),
            request.nombre(),
            request.valorEstimado(),
            request.probabilidad(),
            request.fechaCierreEsperada(),
            request.tipoContrato()
        );
    }

    /**
     * Maps a path id to a delete command.
     */
    public static DeleteTratoCommand toDeleteCommand(UUID id) {
        return new DeleteTratoCommand(id);
    }

    /**
     * Maps a path id to a get-by-id command.
     */
    public static GetTratoByIdCommand toGetByIdCommand(UUID id) {
        return new GetTratoByIdCommand(id);
    }
}