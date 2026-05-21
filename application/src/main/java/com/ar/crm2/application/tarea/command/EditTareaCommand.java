package com.ar.crm2.application.tarea.command;

import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command to edit an existing Tarea.
 * Validates id and titulo at construction time.
 * Does NOT include id, tratoId, creadoEn, fechaCompletada — those are preserved from the existing entity.
 */
public record EditTareaCommand(
    UUID id,
    UUID responsableId,
    String titulo,
    String descripcion,
    TipoTarea tipo,
    PrioridadTarea prioridad,
    LocalDateTime fechaLimite
) {

    public EditTareaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("titulo is required");
        }
    }
}