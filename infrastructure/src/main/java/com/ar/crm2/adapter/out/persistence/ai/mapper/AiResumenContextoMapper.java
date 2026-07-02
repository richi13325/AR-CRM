package com.ar.crm2.adapter.out.persistence.ai.mapper;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiResumenContextoJpaEntity;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiResumenContextoId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity for AI context
 * summaries. UUID ↔ String conversion happens at the persistence
 * boundary, mirroring the project convention used by
 * {@code EmpresaMapper}, {@code ContactoMapper} and the existing
 * {@code AiAccionMapper}.
 */
public final class AiResumenContextoMapper {

    private AiResumenContextoMapper() {}

    /**
     * Maps a domain {@link AiResumenContexto} to a persistence entity.
     * Used for save operations.
     */
    public static AiResumenContextoJpaEntity toEntity(AiResumenContexto domain) {
        return AiResumenContextoJpaEntity.builder()
            .id(domain.getId().value().toString())
            .actorUsuarioId(domain.getActorUsuarioId().value().toString())
            .empresaId(domain.getEmpresaId().value().toString())
            .waConversacionId(domain.getWaConversacionId())
            .contactoId(domain.getContactoId() != null ? domain.getContactoId().value().toString() : null)
            .facts(domain.getFacts())
            .inferences(domain.getInferences())
            .sourceWaMensajeId(domain.getSourceWaMensajeId())
            .sourceWatermark(domain.getSourceWatermark())
            .aiConversacionId(domain.getAiConversacionId().value().toString())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain {@link AiResumenContexto}.
     * Used for find/load operations.
     */
    public static AiResumenContexto toDomain(AiResumenContextoJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AiResumenContexto.reconstitute(
            AiResumenContextoId.from(UUID.fromString(entity.getId())),
            UsuarioId.from(UUID.fromString(entity.getActorUsuarioId())),
            EmpresaId.from(UUID.fromString(entity.getEmpresaId())),
            entity.getWaConversacionId(),
            entity.getContactoId() != null ? ContactoId.from(UUID.fromString(entity.getContactoId())) : null,
            entity.getFacts(),
            entity.getInferences(),
            entity.getSourceWaMensajeId(),
            entity.getSourceWatermark(),
            AiConversacionId.from(UUID.fromString(entity.getAiConversacionId())),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}
