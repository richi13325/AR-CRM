package com.ar.crm2.adapter.out.persistence.ai.mapper;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMensajeJpaEntity;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiMensajeId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity for AI messages.
 *
 * <p>UUID ↔ String conversion happens at the persistence boundary,
 * mirroring the project convention used by {@code EmpresaMapper},
 * {@code ContactoMapper} and the existing {@code AiAccionMapper}.
 */
public final class AiMensajeMapper {

    private AiMensajeMapper() {}

    /**
     * Maps a domain {@link AiMensaje} to a persistence entity.
     * Used for save operations.
     */
    public static AiMensajeJpaEntity toEntity(AiMensaje domain) {
        return AiMensajeJpaEntity.builder()
            .id(domain.getId().value().toString())
            .aiConversacionId(domain.getAiConversacionId().value().toString())
            .rol(domain.getRol())
            .contenido(domain.getContenido())
            .modelo(domain.getModelo())
            .promptTokens(domain.getPromptTokens())
            .completionTokens(domain.getCompletionTokens())
            .latencyMs(domain.getLatencyMs())
            .toolCallJson(domain.getToolCallJson())
            .creadoEn(domain.getCreadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain {@link AiMensaje}.
     * Used for find/load operations.
     */
    public static AiMensaje toDomain(AiMensajeJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AiMensaje.reconstitute(
            AiMensajeId.from(UUID.fromString(entity.getId())),
            AiConversacionId.from(UUID.fromString(entity.getAiConversacionId())),
            entity.getRol(),
            entity.getContenido(),
            entity.getModelo(),
            entity.getPromptTokens(),
            entity.getCompletionTokens(),
            entity.getLatencyMs(),
            entity.getToolCallJson(),
            entity.getCreadoEn()
        );
    }
}
