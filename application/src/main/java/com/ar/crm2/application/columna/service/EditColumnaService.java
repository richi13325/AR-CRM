package com.ar.crm2.application.columna.service;

import com.ar.crm2.application.columna.command.EditColumnaCommand;
import com.ar.crm2.application.columna.exception.ColumnaNotFoundException;
import com.ar.crm2.application.columna.port.in.EditColumnaUseCase;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.application.columna.port.out.FindColumnaByIdPort;
import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.vo.ColumnaId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing EditColumnaUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update via reconstitute,
 * and saving. Preserves all existing fields; only nombre/color/tipoTablero/tipoColumna are updated.
 */
@RequiredArgsConstructor
public class EditColumnaService implements EditColumnaUseCase {

    private final FindColumnaByIdPort findPort;
    private final FindAllColumnasPort findAllPort;
    private final SaveColumnaPort savePort;

    @Override
    public Columna edit(EditColumnaCommand command) {
        ColumnaId columnaId = ColumnaId.from(command.id());

        Columna existing = findPort.findById(columnaId)
                .orElseThrow(() -> ColumnaNotFoundException.forId(command.id()));

        boolean existeDuplicado = ColumnaNamePolicy.hasDuplicateForEdit(
            findAllPort.findAll(),
            columnaId,
            command.tipoTablero(),
            command.nombre()
        );

        Columna updated = Columna.reconstitute(
                existing.getId(),
                command.nombre(),
                command.color(),
                command.tipoTablero(),
                command.tipoColumna(),
                existeDuplicado
        );

        return savePort.save(updated);
    }
}
