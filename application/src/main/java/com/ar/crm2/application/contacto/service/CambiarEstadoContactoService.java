package com.ar.crm2.application.contacto.service;

import com.ar.crm2.application.contacto.command.CambiarEstadoContactoCommand;
import com.ar.crm2.application.contacto.exception.ContactoNotFoundException;
import com.ar.crm2.application.contacto.port.in.CambiarEstadoContactoUseCase;
import com.ar.crm2.application.contacto.port.out.ExistsTratosByContactoIdPort;
import com.ar.crm2.application.contacto.port.out.FindContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.SaveContactoPort;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CambiarEstadoContactoService implements CambiarEstadoContactoUseCase {

    private final FindContactoByIdPort findPort;
    private final SaveContactoPort savePort;
    private final ExistsTratosByContactoIdPort existsTratosPort;

    @Override
    public Contacto cambiarEstado(CambiarEstadoContactoCommand command) {
        ContactoId contactoId = ContactoId.from(command.contactoId());

        Contacto existing = findPort.findById(contactoId)
                .orElseThrow(() -> ContactoNotFoundException.forId(command.contactoId()));

        boolean tieneTratosActivos = existsTratosPort.existsTratosByContactoId(contactoId);

        Contacto updated = existing.cambiarEstadoRelacion(
                command.nuevoEstado(),
                tieneTratosActivos
        );

        return savePort.save(updated);
    }
}
