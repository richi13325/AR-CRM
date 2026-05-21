package com.ar.crm2.application.contacto.service;

import com.ar.crm2.application.contacto.command.EditContactoCommand;
import com.ar.crm2.application.contacto.exception.ContactoNotFoundException;
import com.ar.crm2.application.contacto.port.in.EditContactoUseCase;
import com.ar.crm2.application.contacto.port.out.FindContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.SaveContactoPort;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application service implementing EditContactoUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update, and saving.
 */
@RequiredArgsConstructor
public class EditContactoService implements EditContactoUseCase {

    private final FindContactoByIdPort findPort;
    private final SaveContactoPort savePort;

    @Override
    public Contacto edit(EditContactoCommand command) {
        ContactoId contactoId = ContactoId.from(command.id());

        Contacto existing = findPort.findById(contactoId)
                .orElseThrow(() -> ContactoNotFoundException.forId(command.id()));

        Contacto updated = Contacto.reconstitute(
                existing.getId(),
                existing.getEmpresaId(),
                command.responsableId() != null ? UsuarioId.from(command.responsableId()) : null,
                existing.getCreadoPor(),
                command.nombre(),
                command.correo(),
                command.telefono(),
                command.cargo(),
                command.comoNosConocio(),
                existing.getCreadoEn(),
                LocalDateTime.now(),
                command.estadoRelacion()
        );

        return savePort.save(updated);
    }
}