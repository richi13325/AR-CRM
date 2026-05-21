package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST request DTO for editing an existing Tarea.
 * Same editable fields as CreateTareaRequest; no id/tratoId/timestamps.
 * Timestamps (creadoEn, actualizadoEn, fechaCompletada) and tratoId are never accepted from the client.
 */
public record EditTareaRequest(
    @NotNull(message = "responsableId is required")
    UUID responsableId,

    @NotBlank(message = "titulo is required")
    @Size(min = 1, max = 200, message = "titulo must be 1-200 characters")
    String titulo,

    String descripcion,

    @NotNull(message = "tipo is required")
    TipoTarea tipo,

    @NotNull(message = "prioridad is required")
    PrioridadTarea prioridad,

    @NotNull(message = "fechaLimite is required")
    LocalDateTime fechaLimite
) {}