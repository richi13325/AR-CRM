package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.TareaEntity;
import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class TareaMapper {

    private TareaMapper() {}

    /**
     * Maps a domain Tarea to a persistence entity.
     * Used for save operations.
     */
    public static TareaEntity toEntity(Tarea domain) {
        return TareaEntity.builder()
            .id(domain.getId().value().toString())
            .tratoId(domain.getTratoId().value().toString())
            .responsableId(domain.getResponsableId().value().toString())
            .titulo(domain.getTitulo())
            .descripcion(domain.getDescripcion())
            .tipo(domain.getTipo())
            .prioridad(domain.getPrioridad())
            .fechaLimite(domain.getFechaLimite())
            .fechaCompletada(domain.getFechaCompletada())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Tarea.
     * Used for find/load operations.
     */
    public static Tarea toDomain(TareaEntity entity) {
        if (entity == null) return null;
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Tarea id must not be null");
        }
        if (entity.getTratoId() == null) {
            throw new IllegalArgumentException("Tarea tratoId must not be null");
        }
        if (entity.getResponsableId() == null) {
            throw new IllegalArgumentException("Tarea responsableId must not be null");
        }

        return Tarea.reconstitute(
            TareaId.from(java.util.UUID.fromString(entity.getId())),
            TratoId.from(java.util.UUID.fromString(entity.getTratoId())),
            UsuarioId.from(java.util.UUID.fromString(entity.getResponsableId())),
            entity.getTitulo(),
            entity.getDescripcion(),
            entity.getTipo(),
            entity.getPrioridad(),
            entity.getFechaLimite(),
            entity.getFechaCompletada(),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}