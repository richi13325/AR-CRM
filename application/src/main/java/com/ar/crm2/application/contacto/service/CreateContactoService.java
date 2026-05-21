package com.ar.crm2.application.contacto.service;

import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.contacto.port.out.SaveContactoPort;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateContactoUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveContactoPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateContactoService implements CreateContactoUseCase {

    private final SaveContactoPort savePort;

    @Override
    public Contacto create(CreateContactoCommand command) {
        Contacto contacto = Contacto.create(
            EmpresaId.from(command.empresaId()),
            command.nombre(),
            command.correo(),
            command.estadoRelacion(),
            command.responsableId() != null ? UsuarioId.from(command.responsableId()) : null,
            command.creadoPor() != null ? UsuarioId.from(command.creadoPor()) : null,
            command.telefono(),
            command.cargo(),
            command.comoNosConocio()
        );
        return savePort.save(contacto);
    }
}