package com.ar.crm2.application.tarea.service;

import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.application.tablero.port.out.FindInitialColumnPort;
import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.tarea.port.out.SaveTareaPort;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateTareaUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveTareaPort.
 * Also automatically creates a Kanban Ficha in the initial column of the global TAREA board.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateTareaService implements CreateTareaUseCase {

    private final SaveTareaPort savePort;
    private final SaveFichaPort saveFichaPort;
    private final FindInitialColumnPort findInitialColumnPort;

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
        Tarea savedTarea = savePort.save(tarea);

        Columna columnaInicial = findInitialColumnPort.findInitialColumn(TipoTablero.TAREAS)
            .orElseThrow(() -> new IllegalStateException(
                "No initial column found for global TAREAS board. Cannot create Tarea without Kanban Ficha."
            ));

        Ficha ficha = Ficha.create(
            columnaInicial.getId(),
            TipoFicha.TAREA,
            null,
            savedTarea.getId()
        );
        saveFichaPort.save(ficha);

        return savedTarea;
    }
}
