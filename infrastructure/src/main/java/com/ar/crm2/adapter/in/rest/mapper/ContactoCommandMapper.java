package com.ar.crm2.adapter.in.rest.mapper;

import com.ar.crm2.adapter.in.rest.dto.request.CreateContactoRequest;
import com.ar.crm2.adapter.in.rest.dto.request.EditContactoRequest;
import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.contacto.command.DeleteContactoCommand;
import com.ar.crm2.application.contacto.command.EditContactoCommand;
import com.ar.crm2.application.contacto.command.GetContactoByIdCommand;

import java.util.UUID;

/**
 * Mapper from REST DTOs to application commands.
 */
public final class ContactoCommandMapper {

    private ContactoCommandMapper() {}

    /**
     * Maps a REST create request to an application command.
     */
    public static CreateContactoCommand toCommand(CreateContactoRequest request) {
        return new CreateContactoCommand(
            request.empresaId(),
            request.nombre(),
            request.correo(),
            request.estadoRelacion(),
            request.responsableId(),
            request.creadoPor(),
            request.telefono(),
            request.cargo(),
            request.comoNosConocio()
        );
    }

    /**
     * Maps a REST edit request with a path id to an application command.
     */
    public static EditContactoCommand toCommand(UUID id, EditContactoRequest request) {
        return new EditContactoCommand(
            id,
            request.nombre(),
            request.correo(),
            request.estadoRelacion(),
            request.responsableId(),
            request.telefono(),
            request.cargo(),
            request.comoNosConocio()
        );
    }

    /**
     * Maps a path id to a delete command.
     */
    public static DeleteContactoCommand toDeleteCommand(UUID id) {
        return new DeleteContactoCommand(id);
    }

    /**
     * Maps a path id to a get-by-id command.
     */
    public static GetContactoByIdCommand toGetByIdCommand(UUID id) {
        return new GetContactoByIdCommand(id);
    }
}