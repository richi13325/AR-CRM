package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.SuperUsuarioEntity;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class SuperUsuarioMapper {

    private SuperUsuarioMapper() {}

    /**
     * Maps a domain SuperUsuario to a persistence entity.
     * Used for save operations.
     */
    public static SuperUsuarioEntity toEntity(SuperUsuario domain) {
        if (domain == null) {
            return null;
        }
        return SuperUsuarioEntity.builder()
            .id(domain.getId().value().toString())
            .correo(domain.getCorreo())
            .passwordHash(domain.getPasswordHash())
            .creadoEn(domain.getCreadoEn())
            .activo(domain.isActivo())
            .build();
    }

    /**
     * Maps a persistence entity to a domain SuperUsuario.
     * Used for find/load operations.
     */
    public static SuperUsuario toDomain(SuperUsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("SuperUsuario id must not be null");
        }
        return SuperUsuario.reconstitute(
            SuperUsuarioId.from(java.util.UUID.fromString(entity.getId())),
            entity.getCorreo(),
            entity.getPasswordHash(),
            entity.getCreadoEn(),
            entity.isActivo()
        );
    }
}