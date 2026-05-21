package com.ar.crm2.application.contacto.service;

import com.ar.crm2.application.contacto.command.GetContactoByIdCommand;
import com.ar.crm2.application.contacto.exception.ContactoNotFoundException;
import com.ar.crm2.application.contacto.port.in.GetContactoByIdUseCase;
import com.ar.crm2.application.contacto.port.out.FindContactoByIdPort;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetContactoByIdUseCase.
 * Loads a Contacto by id or throws ContactoNotFoundException.
 */
@RequiredArgsConstructor
public class GetContactoByIdService implements GetContactoByIdUseCase {

    private final FindContactoByIdPort findPort;

    @Override
    public Contacto getById(GetContactoByIdCommand command) {
        ContactoId contactoId = ContactoId.from(command.id());

        return findPort.findById(contactoId)
                .orElseThrow(() -> ContactoNotFoundException.forId(command.id()));
    }
}