package com.ar.crm2.application.tarea.command;

import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command to create a new Tarea.
 * Required fields validated at construction time.
 */
public record CreateTareaCommand(
    UUID tratoId,
    UUID responsableId,
    String titulo,
    String descripcion,
    TipoTarea tipo,
    PrioridadTarea prioridad,
    LocalDateTime fechaLimite
) {

    public CreateTareaCommand {
        if (tratoId == null) {
            throw new IllegalArgumentException("tratoId is required");
        }
        if (responsableId == null) {
            throw new IllegalArgumentException("responsableId is required");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("titulo is required");
        }
    }
}