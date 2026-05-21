package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoTarea;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST response DTO for Tarea.
 * Exposes all fields needed for front-end list/create/edit views.
 */
public record TareaResponse(
    UUID id,
    UUID tratoId,
    UUID responsableId,
    String titulo,
    String descripcion,
    TipoTarea tipo,
    PrioridadTarea prioridad,
    LocalDateTime fechaLimite,
    LocalDateTime fechaCompletada,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
    /**
     * Maps a domain Tarea to this response DTO.
     */
    public static TareaResponse fromDomain(Tarea tarea) {
        return new TareaResponse(
            tarea.getId().value(),
            tarea.getTratoId().value(),
            tarea.getResponsableId().value(),
            tarea.getTitulo(),
            tarea.getDescripcion(),
            tarea.getTipo(),
            tarea.getPrioridad(),
            tarea.getFechaLimite(),
            tarea.getFechaCompletada(),
            tarea.getCreadoEn(),
            tarea.getActualizadoEn()
        );
    }
}