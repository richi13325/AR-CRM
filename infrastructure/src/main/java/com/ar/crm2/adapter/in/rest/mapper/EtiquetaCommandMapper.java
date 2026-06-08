package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateEtiquetaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditEtiquetaRequest;
import com.ar.crm2.application.etiqueta.command.CreateEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.DeleteEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.EditEtiquetaCommand;
import com.ar.crm2.application.etiqueta.command.GetAllEtiquetasCommand;
import com.ar.crm2.application.etiqueta.command.GetEtiquetaByIdCommand;
import com.ar.crm2.model.enums.TipoEtiqueta;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands for Etiqueta operations.
 * Pure static utility — no Spring dependencies, fully unit-testable.
 */
public final class EtiquetaCommandMapper {

    private EtiquetaCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateEtiquetaCommand toCreateCommand(CreateEtiquetaRequest request) {
        return new CreateEtiquetaCommand(
            request.nombre(),
            request.tipoEtiqueta(),
            request.color()
        );
    }

    /**
     * Maps a REST edit request with a query-parameter id to an application command.
     */
    public static EditEtiquetaCommand toEditCommand(UUID id, EditEtiquetaRequest request) {
        return new EditEtiquetaCommand(
            id,
            request.nombre(),
            request.color()
        );
    }

    /**
     * Maps a query-parameter id to a get-by-id command.
     */
    public static GetEtiquetaByIdCommand toGetByIdCommand(UUID id) {
        return new GetEtiquetaByIdCommand(id);
    }

    /**
     * Maps a query-parameter id and confirm flag to a delete command.
     */
    public static DeleteEtiquetaCommand toDeleteCommand(UUID id, boolean confirm) {
        return new DeleteEtiquetaCommand(id, confirm);
    }

    /**
     * Maps a query-parameter tipo filter (nullable) to a get-all command.
     */
    public static GetAllEtiquetasCommand toGetAllCommand(TipoEtiqueta tipo) {
        return new GetAllEtiquetasCommand(tipo);
    }
}
