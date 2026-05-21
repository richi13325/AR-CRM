package com.ar.crm2.application.tarea.service;

import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.tarea.port.out.SaveTareaPort;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateTareaUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveTareaPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateTareaService implements CreateTareaUseCase {

    private final SaveTareaPort savePort;

    @Override
    public Tarea create(CreateTareaCommand command) {
        Tarea tarea = Tarea.create(
            TratoId.from(command.tratoId()),
            UsuarioId.from(command.responsableId()),
            command.titulo(),
            command.descripcion(),
            command.tipo(),
            command.prioridad(),
            command.fechaLimite()
        );
        return savePort.save(tarea);
    }
}