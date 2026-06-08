package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.EtiquetaEntity;
import com.ar.crm2.model.entity.Etiqueta;
import com.ar.crm2.model.vo.EtiquetaId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity for the Etiqueta catalog.
 * Handles UUID/String conversion at the persistence boundary.
 *
 * <p>Static utility — no repository dependencies (the Etiqueta aggregate is
 * self-contained, unlike {@code TableroMapper} which needs the
 * {@code ColumnaRepository} to hydrate the catalog column on read).
 */
public final class EtiquetaMapper {

    private EtiquetaMapper() {
    }

    /**
     * Maps a domain Etiqueta to a persistence entity.
     * Used for save operations.
     */
    public static EtiquetaEntity toEntity(Etiqueta domain) {
        if (domain == null) {
            return null;
        }
        return EtiquetaEntity.builder()
            .id(domain.getId().value().toString())
            .nombre(domain.getNombre())
            .tipoEtiqueta(domain.getTipoEtiqueta())
            .color(domain.getColor())
            .creadoEn(domain.getCreadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Etiqueta.
     * Used for find/load operations.
     */
    public static Etiqueta toDomain(EtiquetaEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Etiqueta id must not be null");
        }
        if (entity.getCreadoEn() == null) {
            throw new IllegalArgumentException("Etiqueta creadoEn must not be null");
        }
        return Etiqueta.reconstitute(
            EtiquetaId.from(UUID.fromString(entity.getId())),
            entity.getNombre(),
            entity.getTipoEtiqueta(),
            entity.getColor(),
            entity.getCreadoEn()
        );
    }
}
