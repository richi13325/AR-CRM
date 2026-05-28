package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditUsuarioRequest;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.command.DeleteUsuarioCommand;
import com.ar.crm2.application.usuario.command.EditUsuarioCommand;
import com.ar.crm2.application.usuario.command.GetUsuarioByIdCommand;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class UsuarioCommandMapper {

    private UsuarioCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateUsuarioCommand toCommand(CreateUsuarioRequest request) {
        return new CreateUsuarioCommand(
            request.nombre(),
            request.correo(),
            request.rolId(),
            request.keycloakId(),
            request.initialPassword()
        );
    }

    /**
     * Maps an edit request with a query-parameter id to an application command.
     */
    public static EditUsuarioCommand toCommand(UUID id, EditUsuarioRequest request) {
        return new EditUsuarioCommand(
            id,
            request.nombre(),
            request.correo(),
            request.rolId(),
            request.keycloakId()
        );
    }

    /**
     * Maps a query-parameter id to a delete command.
     */
    public static DeleteUsuarioCommand toDeleteCommand(UUID id) {
        return new DeleteUsuarioCommand(id);
    }

    /**
     * Maps a query-parameter id to a get-by-id command.
     */
    public static GetUsuarioByIdCommand toGetByIdCommand(UUID id) {
        return new GetUsuarioByIdCommand(id);
    }
}