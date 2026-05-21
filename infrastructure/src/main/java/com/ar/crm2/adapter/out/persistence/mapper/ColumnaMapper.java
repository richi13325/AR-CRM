package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.ColumnaEntity;
import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class ColumnaMapper {

    private ColumnaMapper() {}

    /**
     * Maps a domain Columna to a persistence entity.
     * Used for save operations.
     */
    public static ColumnaEntity toEntity(Columna domain) {
        if (domain == null) {
            return null;
        }
        return ColumnaEntity.builder()
            .id(domain.getId().value().toString())
            .columnaNombre(domain.getColumnanombre())
            .color(domain.getColor())
            .tipoTablero(domain.getTipoTablero().name())
            .tipoColumna(domain.getTipoColumna().name())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Columna.
     * Used for find/load operations.
     */
    public static Columna toDomain(ColumnaEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Columna id must not be null");
        }
        return Columna.reconstitute(
            ColumnaId.from(UUID.fromString(entity.getId())),
            entity.getColumnaNombre(),
            entity.getColor(),
            TipoTablero.valueOf(entity.getTipoTablero()),
            TipoColumna.valueOf(entity.getTipoColumna()),
            false
        );
    }
}