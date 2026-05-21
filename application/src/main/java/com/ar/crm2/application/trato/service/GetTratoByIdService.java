package com.ar.crm2.application.trato.service;

import com.ar.crm2.application.trato.command.GetTratoByIdCommand;
import com.ar.crm2.application.trato.exception.TratoNotFoundException;
import com.ar.crm2.application.trato.port.in.GetTratoByIdUseCase;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.vo.TratoId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetTratoByIdUseCase.
 * Loads a Trato by id or throws TratoNotFoundException.
 */
@RequiredArgsConstructor
public class GetTratoByIdService implements GetTratoByIdUseCase {

    private final FindTratoByIdPort findPort;

    @Override
    public Trato getById(GetTratoByIdCommand command) {
        TratoId tratoId = TratoId.from(command.id());

        return findPort.findById(tratoId)
                .orElseThrow(() -> TratoNotFoundException.forId(command.id()));
    }
}