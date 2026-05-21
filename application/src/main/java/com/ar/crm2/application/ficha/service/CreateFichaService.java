package com.ar.crm2.application.ficha.service;

import com.ar.crm2.application.ficha.command.CreateFichaCommand;
import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.application.ficha.port.in.CreateFichaUseCase;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateFichaUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveFichaPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateFichaService implements CreateFichaUseCase {

    private final SaveFichaPort savePort;

    @Override
    public Ficha create(CreateFichaCommand command) {
        Ficha ficha = Ficha.create(
            ColumnaId.from(command.columnaId()),
            command.tipoFicha(),
            command.tratoId() != null ? TratoId.from(command.tratoId()) : null,
            command.tareaId() != null ? TareaId.from(command.tareaId()) : null,
            UsuarioId.from(command.responsableId()),
            UsuarioId.from(command.creadoPor())
        );
        return savePort.save(ficha);
    }
}