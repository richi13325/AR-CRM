package com.ar.crm2.adapter.out.persistence.ai.mapper;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMemoriaJpaEntity;
import com.ar.crm2.model.entity.ia.AiMemoria;
import com.ar.crm2.model.vo.AiMemoriaId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity for AI memory.
 * UUID ↔ String conversion happens at the persistence boundary,
 * mirroring the project convention used by {@code EmpresaMapper},
 * {@code ContactoMapper} and the existing {@code AiAccionMapper}.
 */
public final class AiMemoriaMapper {

    private AiMemoriaMapper() {}

    /**
     * Maps a domain {@link AiMemoria} to a persistence entity.
     * Used for save operations.
     */
    public static AiMemoriaJpaEntity toEntity(AiMemoria domain) {
        return AiMemoriaJpaEntity.builder()
            .id(domain.getId().value().toString())
            .actorUsuarioId(domain.getActorUsuarioId().value().toString())
            .empresaId(domain.getEmpresaId().value().toString())
            .waConversacionId(domain.getWaConversacionId())
            .contactoId(domain.getContactoId() != null ? domain.getContactoId().value().toString() : null)
            .visibilidad(domain.getVisibilidad())
            .contenido(domain.getContenido())
            .origenTipo(domain.getOrigenTipo())
            .origenId(domain.getOrigenId())
            .version(domain.getVersion())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .expiresAt(domain.getExpiresAt())
            .supersededBy(domain.getSupersededBy() != null ? domain.getSupersededBy().value().toString() : null)
            .superseded(domain.isSuperseded())
            .expirada(domain.isExpirada())
            .build();
    }

    /**
     * Maps a persistence entity to a domain {@link AiMemoria}.
     * Used for find/load operations.
     */
    public static AiMemoria toDomain(AiMemoriaJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AiMemoria.reconstitute(
            AiMemoriaId.from(UUID.fromString(entity.getId())),
            UsuarioId.from(UUID.fromString(entity.getActorUsuarioId())),
            EmpresaId.from(UUID.fromString(entity.getEmpresaId())),
            entity.getWaConversacionId(),
            entity.getContactoId() != null ? ContactoId.from(UUID.fromString(entity.getContactoId())) : null,
            entity.getVisibilidad(),
            entity.getContenido(),
            entity.getOrigenTipo(),
            entity.getOrigenId(),
            entity.getVersion(),
            entity.getCreadoEn(),
            entity.getActualizadoEn(),
            entity.getExpiresAt(),
            entity.getSupersededBy() != null ? AiMemoriaId.from(UUID.fromString(entity.getSupersededBy())) : null,
            entity.isSuperseded(),
            entity.isExpirada()
        );
    }
}
