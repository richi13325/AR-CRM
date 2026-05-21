package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.RolEntity;
import com.ar.crm2.model.entity.Rol;
import com.ar.crm2.model.vo.RolId;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class RolMapper {

    private RolMapper() {}

    /**
     * Maps a domain Rol to a persistence entity.
     * Used for save operations.
     */
    public static RolEntity toEntity(Rol domain) {
        if (domain == null) {
            return null;
        }
        return RolEntity.builder()
            .id(domain.getId().value().toString())
            .nombre(domain.getNombre())
            .descripcion(domain.getDescripcion())
            .activo(domain.isActivo())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Rol.
     * Used for find/load operations.
     */
    public static Rol toDomain(RolEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Rol id must not be null");
        }
        return Rol.reconstitute(
            RolId.from(java.util.UUID.fromString(entity.getId())),
            entity.getNombre(),
            entity.getDescripcion(),
            entity.isActivo()
        );
    }
}