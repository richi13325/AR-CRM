package com.ar.crm2.application.ficha.service;

import com.ar.crm2.application.ficha.command.GetFichaByIdCommand;
import com.ar.crm2.application.ficha.exception.FichaNotFoundException;
import com.ar.crm2.application.ficha.port.in.GetFichaByIdUseCase;
import com.ar.crm2.application.ficha.port.out.FindFichaByIdPort;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.FichaId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetFichaByIdUseCase.
 * Loads a Ficha by id or throws FichaNotFoundException.
 */
@RequiredArgsConstructor
public class GetFichaByIdService implements GetFichaByIdUseCase {

    private final FindFichaByIdPort findPort;

    @Override
    public Ficha getById(GetFichaByIdCommand command) {
        FichaId fichaId = FichaId.from(command.id());

        return findPort.findById(fichaId)
                .orElseThrow(() -> FichaNotFoundException.forId(command.id()));
    }
}