package com.ar.crm2.application.trato.service;

import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.application.trato.port.out.SaveTratoPort;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateTratoUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveTratoPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateTratoService implements CreateTratoUseCase {

    private final SaveTratoPort savePort;

    @Override
    public Trato create(CreateTratoCommand command) {
        Trato trato = Trato.create(
            ContactoId.from(command.contactoId()),
            UsuarioId.from(command.responsableId()),
            command.nombre(),
            command.valorEstimado(),
            command.probabilidad(),
            command.fechaCierreEsperada(),
            command.tipoContrato()
        );
        return savePort.save(trato);
    }
}