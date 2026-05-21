package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST request DTO for creating a new Tarea.
 * Required fields validated at construction time.
 * Timestamps (creadoEn, actualizadoEn, fechaCompletada) and id are never accepted from the client.
 */
public record CreateTareaRequest(
    @NotNull(message = "tratoId is required")
    UUID tratoId,

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