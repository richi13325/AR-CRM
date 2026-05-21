package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateSuperUsuarioRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditSuperUsuarioRequest;
import com.ar.crm2.application.superusuario.command.CreateSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.command.DeleteSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.command.EditSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.command.GetSuperUsuarioByIdCommand;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class SuperUsuarioCommandMapper {

    private SuperUsuarioCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateSuperUsuarioCommand toCommand(CreateSuperUsuarioRequest request) {
        return new CreateSuperUsuarioCommand(
            request.correo(),
            request.passwordHash()
        );
    }

    /**
     * Maps an edit request with a query-parameter id to an application command.
     */
    public static EditSuperUsuarioCommand toCommand(UUID id, EditSuperUsuarioRequest request) {
        return new EditSuperUsuarioCommand(id, request.correo());
    }

    /**
     * Maps a query-parameter id to a delete command.
     */
    public static DeleteSuperUsuarioCommand toDeleteCommand(UUID id) {
        return new DeleteSuperUsuarioCommand(id);
    }

    /**
     * Maps a query-parameter id to a get-by-id command.
     */
    public static GetSuperUsuarioByIdCommand toGetByIdCommand(UUID id) {
        return new GetSuperUsuarioByIdCommand(id);
    }
}
