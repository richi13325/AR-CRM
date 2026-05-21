package com.ar.crm2.application.ficha.service;

import com.ar.crm2.application.ficha.command.DeleteFichaCommand;
import com.ar.crm2.application.ficha.exception.FichaNotFoundException;
import com.ar.crm2.application.ficha.port.in.DeleteFichaUseCase;
import com.ar.crm2.application.ficha.port.out.DeleteFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.FindFichaByIdPort;
import com.ar.crm2.model.vo.FichaId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing DeleteFichaUseCase.
 * Validates existence before hard-deleting.
 */
@RequiredArgsConstructor
public class DeleteFichaService implements DeleteFichaUseCase {

    private final FindFichaByIdPort findPort;
    private final DeleteFichaByIdPort deletePort;

    @Override
    public void delete(DeleteFichaCommand command) {
        FichaId fichaId = FichaId.from(command.id());

        // Verify ficha exists
        findPort.findById(fichaId)
                .orElseThrow(() -> FichaNotFoundException.forId(command.id()));

        deletePort.deleteById(fichaId);
    }
}