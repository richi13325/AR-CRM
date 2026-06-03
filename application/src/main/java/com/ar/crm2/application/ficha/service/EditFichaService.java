package com.ar.crm2.application.ficha.service;

import com.ar.crm2.application.ficha.command.EditFichaCommand;
import com.ar.crm2.application.ficha.exception.FichaNotFoundException;
import com.ar.crm2.application.ficha.port.in.EditFichaUseCase;
import com.ar.crm2.application.ficha.port.out.FindFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.TareaId;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Application service implementing EditFichaUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update via reconstitute,
 * and saving.
 */
@RequiredArgsConstructor
public class EditFichaService implements EditFichaUseCase {

    private final FindFichaByIdPort findPort;
    private final SaveFichaPort savePort;

    @Override
    public Ficha edit(EditFichaCommand command) {
        FichaId fichaId = FichaId.from(command.id());

        Ficha existing = findPort.findById(fichaId)
                .orElseThrow(() -> FichaNotFoundException.forId(command.id()));

        Ficha updated = Ficha.reconstitute(
                existing.getId(),
                ColumnaId.from(command.columnaId()),
                command.tipoFicha(),
                command.tratoId() != null ? TratoId.from(command.tratoId()) : null,
                command.tareaId() != null ? TareaId.from(command.tareaId()) : null,
                Instant.now()
        );

        return savePort.save(updated);
    }
}