package com.ar.crm2.application.columna.service;

import com.ar.crm2.application.columna.command.GetColumnaByIdCommand;
import com.ar.crm2.application.columna.exception.ColumnaNotFoundException;
import com.ar.crm2.application.columna.port.in.GetColumnaByIdUseCase;
import com.ar.crm2.application.columna.port.out.FindColumnaByIdPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetColumnaByIdUseCase.
 * Loads a Columna by id or throws ColumnaNotFoundException.
 */
@RequiredArgsConstructor
public class GetColumnaByIdService implements GetColumnaByIdUseCase {

    private final FindColumnaByIdPort findPort;

    @Override
    public Columna getById(GetColumnaByIdCommand command) {
        ColumnaId columnaId = ColumnaId.from(command.id());

        return findPort.findById(columnaId)
                .orElseThrow(() -> ColumnaNotFoundException.forId(command.id()));
    }
}