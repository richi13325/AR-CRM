package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.FichaEntity;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.UsuarioId;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class FichaMapper {

    private FichaMapper() {}

    /**
     * Maps a domain Ficha to a persistence entity.
     * Used for save operations.
     */
    public static FichaEntity toEntity(Ficha domain) {
        if (domain == null) {
            return null;
        }
        return FichaEntity.builder()
            .id(domain.getId().value().toString())
            .columnaId(domain.getColumnaId().value().toString())
            .tipoFicha(domain.getTipoFicha())
            .tratoId(domain.getTratoId() != null ? domain.getTratoId().value().toString() : null)
            .tareaId(domain.getTareaId() != null ? domain.getTareaId().value().toString() : null)
            .responsableId(domain.getResponsableId().value().toString())
            .creadoPor(domain.getCreadoPor().value().toString())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Ficha.
     * Used for find/load operations.
     */
    public static Ficha toDomain(FichaEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Ficha id must not be null");
        }
        if (entity.getColumnaId() == null) {
            throw new IllegalArgumentException("Ficha columnaId must not be null");
        }
        if (entity.getResponsableId() == null) {
            throw new IllegalArgumentException("Ficha responsableId must not be null");
        }
        if (entity.getCreadoPor() == null) {
            throw new IllegalArgumentException("Ficha creadoPor must not be null");
        }

        return Ficha.reconstitute(
            FichaId.from(java.util.UUID.fromString(entity.getId())),
            ColumnaId.from(java.util.UUID.fromString(entity.getColumnaId())),
            entity.getTipoFicha(),
            entity.getTratoId() != null ? TratoId.from(java.util.UUID.fromString(entity.getTratoId())) : null,
            entity.getTareaId() != null ? TareaId.from(java.util.UUID.fromString(entity.getTareaId())) : null,
            UsuarioId.from(java.util.UUID.fromString(entity.getResponsableId())),
            UsuarioId.from(java.util.UUID.fromString(entity.getCreadoPor())),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}