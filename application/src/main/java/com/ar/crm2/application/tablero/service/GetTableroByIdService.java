package com.ar.crm2.application.tablero.service;

import com.ar.crm2.application.tablero.command.GetTableroByIdCommand;
import com.ar.crm2.application.tablero.exception.TableroNotFoundException;
import com.ar.crm2.application.tablero.port.in.GetTableroByIdUseCase;
import com.ar.crm2.application.tablero.port.out.FindTableroByIdPort;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.vo.TableroId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetTableroByIdUseCase.
 * Loads a Tablero by id or throws TableroNotFoundException.
 */
@RequiredArgsConstructor
public class GetTableroByIdService implements GetTableroByIdUseCase {

    private final FindTableroByIdPort findPort;

    @Override
    public Tablero getById(GetTableroByIdCommand command) {
        TableroId tableroId = TableroId.from(command.id());

        return findPort.findById(tableroId)
                .orElseThrow(() -> TableroNotFoundException.forId(command.id()));
    }
}