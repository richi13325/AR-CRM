package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.AsignarColumnaRequest;
import com.ar.crm2.adapter.in.rest.dto.request.CreateTableroRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditTableroRequest;
import com.ar.crm2.adapter.in.rest.dto.request.ReordenarColumnasRequest;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.application.security.exception.AuthenticatedUsuarioRequiredException;
import com.ar.crm2.application.tablero.command.AsignarColumnaTableroCommand;
import com.ar.crm2.application.tablero.command.CreateTableroCommand;
import com.ar.crm2.application.tablero.command.DeleteTableroCommand;
import com.ar.crm2.application.tablero.command.EditTableroCommand;
import com.ar.crm2.application.tablero.command.EliminarColumnaDelTableroCommand;
import com.ar.crm2.application.tablero.command.GetTableroByIdCommand;
import com.ar.crm2.application.tablero.command.ReordenarColumnasCommand;
import com.ar.crm2.model.vo.ColumnaId;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands for Tablero operations.
 */
public final class TableroCommandMapper {

    private TableroCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     * The actor id is derived from the authenticated {@link ActorContext}
     * (token-provided) rather than the request body — eliminating the spoofable field.
     *
     * <p>Authorization: any authenticated actor may create a Tablero. The mapper
     * prefers {@code superUsuarioId} (preserves superusuario path) and falls back
     * to {@code usuarioId} (allows a normal usuario to create). Throws
     * {@link AuthenticatedUsuarioRequiredException} if the actor context is
     * missing or carries neither id claim.
     */
    public static CreateTableroCommand toCommand(CreateTableroRequest request, ActorContext actorContext) {
        if (actorContext == null) {
            throw AuthenticatedUsuarioRequiredException.forMissingActorContext();
        }
        UUID actorId = actorContext.superUsuarioId()
                .or(() -> actorContext.usuarioId())
                .orElseThrow(AuthenticatedUsuarioRequiredException::forMissingUsuarioId);
        return new CreateTableroCommand(
            request.nombre(),
            request.descripcion(),
            request.tipoTablero(),
            request.columnasPredeterminadas(),
            actorId
        );
    }

    /**
     * Maps a REST edit request with a path id to an application command.
     */
    public static EditTableroCommand toCommand(UUID id, EditTableroRequest request) {
        return new EditTableroCommand(
            id,
            request.nombre(),
            request.descripcion()
        );
    }

    /**
     * Maps a path id to a get-by-id command.
     */
    public static GetTableroByIdCommand toGetByIdCommand(UUID id) {
        return new GetTableroByIdCommand(id);
    }

    /**
     * Maps a path id to a delete command.
     */
    public static DeleteTableroCommand toDeleteCommand(UUID id) {
        return new DeleteTableroCommand(id);
    }

    /**
     * Maps a path tableroId and columnaId to an application delete-column command.
     */
    public static EliminarColumnaDelTableroCommand toDeleteColumnCommand(UUID tableroId, UUID columnaId) {
        return new EliminarColumnaDelTableroCommand(tableroId, columnaId);
    }

    /**
     * Maps a REST reorder request to an application command.
     */
    public static ReordenarColumnasCommand toCommand(UUID tableroId, ReordenarColumnasRequest request) {
        return new ReordenarColumnasCommand(
                tableroId,
                request.nuevoOrden().stream()
                        .map(ColumnaId::from)
                        .toList()
        );
    }

    /**
     * Maps a REST assign-column request to an application command.
     *
     * <p>This command does NOT carry column definition fields (nombre, color,
     * tipoColumna). Those belong to the Columna catalog. This overload only
     * carries board-specific context (WIP, note, state).
     */
    public static AsignarColumnaTableroCommand toAsignarCommand(
            UUID tableroId,
            UUID columnaId,
            AsignarColumnaRequest request
    ) {
        return new AsignarColumnaTableroCommand(
            tableroId,
            columnaId,
            request.limiteWip(),
            request.nota(),
            request.estadoTarea(),
            request.estadoTrato(),
            request.totalValorEstimado()
        );
    }
}
