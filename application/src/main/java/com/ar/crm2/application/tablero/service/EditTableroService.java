package com.ar.crm2.application.tablero.service;

import com.ar.crm2.application.tablero.command.EditTableroCommand;
import com.ar.crm2.application.tablero.exception.TableroNotFoundException;
import com.ar.crm2.application.tablero.port.in.EditTableroUseCase;
import com.ar.crm2.application.tablero.port.out.FindTableroByIdPort;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.model.entity.Tablero;
import com.ar.crm2.model.vo.TableroId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing EditTableroUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update, and saving.
 */
@RequiredArgsConstructor
public class EditTableroService implements EditTableroUseCase {

    private final FindTableroByIdPort findPort;
    private final SaveTableroPort savePort;

    @Override
    public Tablero edit(EditTableroCommand command) {
        TableroId tableroId = TableroId.from(command.id());

        Tablero existing = findPort.findById(tableroId)
                .orElseThrow(() -> TableroNotFoundException.forId(command.id()));

        Tablero updated = Tablero.reconstitute(
                existing.getId(),
                command.nombre(),
                command.descripcion(),
                existing.getColumnasTablero(),
                existing.getTipoTablero(),
                existing.getCreadoEn()
        );

        return savePort.save(updated);
    }
}