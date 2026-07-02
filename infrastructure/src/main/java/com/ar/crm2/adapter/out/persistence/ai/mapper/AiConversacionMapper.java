package com.ar.crm2.adapter.out.persistence.ai.mapper;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity for AI conversations.
 *
 * <p>UUID ↔ String conversion happens at the persistence boundary,
 * mirroring the project convention used by {@code EmpresaMapper},
 * {@code ContactoMapper} and the existing {@code AiAccionMapper}.
 */
public final class AiConversacionMapper {

    private AiConversacionMapper() {}

    /**
     * Maps a domain {@link AiConversacion} to a persistence entity.
     * Used for save operations.
     */
    public static AiConversacionJpaEntity toEntity(AiConversacion domain) {
        return AiConversacionJpaEntity.builder()
            .id(domain.getId().value().toString())
            .empresaId(domain.getEmpresaId().value().toString())
            .actorUsuarioId(domain.getActorUsuarioId().value().toString())
            .waConversacionId(domain.getWaConversacionId())
            .contactoId(domain.getContactoId() != null ? domain.getContactoId().value().toString() : null)
            .archivada(domain.isArchivada())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain {@link AiConversacion}.
     * Used for find/load operations.
     */
    public static AiConversacion toDomain(AiConversacionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AiConversacion.reconstitute(
            AiConversacionId.from(UUID.fromString(entity.getId())),
            EmpresaId.from(UUID.fromString(entity.getEmpresaId())),
            UsuarioId.from(UUID.fromString(entity.getActorUsuarioId())),
            entity.getWaConversacionId(),
            entity.getContactoId() != null ? ContactoId.from(UUID.fromString(entity.getContactoId())) : null,
            entity.isArchivada(),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}
