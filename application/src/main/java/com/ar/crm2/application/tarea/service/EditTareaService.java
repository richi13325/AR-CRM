package com.ar.crm2.application.tarea.service;

import com.ar.crm2.application.tarea.command.EditTareaCommand;
import com.ar.crm2.application.tarea.exception.TareaNotFoundException;
import com.ar.crm2.application.tarea.port.in.EditTareaUseCase;
import com.ar.crm2.application.tarea.port.out.FindTareaByIdPort;
import com.ar.crm2.application.tarea.port.out.SaveTareaPort;
import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Application service implementing EditTareaUseCase.
 * Orchestrates loading the aggregate, applying the immutable domain update via reconstitute,
 * and saving. Preserves: id, tratoId, creadoEn, fechaCompletada.
 */
@RequiredArgsConstructor
public class EditTareaService implements EditTareaUseCase {

    private final FindTareaByIdPort findPort;
    private final SaveTareaPort savePort;

    @Override
    public Tarea edit(EditTareaCommand command) {
        TareaId tareaId = TareaId.from(command.id());

        Tarea existing = findPort.findById(tareaId)
                .orElseThrow(() -> TareaNotFoundException.forId(command.id()));

        Tarea updated = Tarea.reconstitute(
                existing.getId(),
                existing.getTratoId(),
                command.responsableId() != null ? UsuarioId.from(command.responsableId()) : null,
                command.titulo(),
                command.descripcion(),
                command.tipo(),
                command.prioridad(),
                command.fechaLimite(),
                existing.getFechaCompletada(),
                existing.getCreadoEn(),
                LocalDateTime.now()
        );

        return savePort.save(updated);
    }
}