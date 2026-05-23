package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.UsuarioEntity;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;

/**
 * Mapper between persistence entity and domain entity.
 * Handles UUID/String conversion at the persistence boundary.
 */
public final class UsuarioMapper {

    private UsuarioMapper() {}

    /**
     * Maps a domain Usuario to a persistence entity.
     * Used for save operations.
     */
    public static UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) {
            return null;
        }
        return UsuarioEntity.builder()
            .id(domain.getId().value().toString())
            .nombre(domain.getNombre())
            .correo(domain.getCorreo())
            .passwordHash(domain.getPasswordHash())
            .rolId(domain.getRolId().value().toString())
            .creadoEn(domain.getCreadoEn())
            .activo(domain.isActivo())
            .keycloakId(domain.getKeycloakId())
            .build();
    }

    /**
     * Maps a persistence entity to a domain Usuario.
     * Used for find/load operations.
     */
    public static Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Usuario id must not be null");
        }
        return Usuario.reconstitute(
            UsuarioId.from(java.util.UUID.fromString(entity.getId())),
            entity.getNombre(),
            entity.getCorreo(),
            entity.getPasswordHash(),
            RolId.from(java.util.UUID.fromString(entity.getRolId())),
            entity.getCreadoEn(),
            entity.isActivo(),
            entity.getKeycloakId()
        );
    }
}