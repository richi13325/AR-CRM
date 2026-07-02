package com.ar.crm2.adapter.out.persistence.ai.mapper;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiAccionJpaEntity;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

/**
 * Mapper between persistence entity and domain entity for AI action
 * proposals. UUID ↔ String conversion happens at the persistence
 * boundary, mirroring the project convention used by
 * {@code EmpresaMapper}, {@code ContactoMapper}, etc.
 *
 * <p>Round-trip also preserves the audit-link fields
 * {@code ai_conversacion_id} and {@code wa_mensaje_id} required by
 * {@code ai-action-proposal/spec.md} "Preserve auditability of AI
 * decisions".
 */
public final class AiAccionMapper {

    private AiAccionMapper() {}

    /**
     * Maps a domain {@link AiAccion} to a persistence entity.
     * Used for save operations.
     */
    public static AiAccionJpaEntity toEntity(AiAccion domain) {
        return AiAccionJpaEntity.builder()
            .id(domain.getId().value().toString())
            .empresaId(domain.getEmpresaId().value().toString())
            .solicitadaPor(domain.getSolicitadaPor().value().toString())
            .waConversacionId(domain.getWaConversacionId())
            .waMensajeId(domain.getWaMensajeId())
            .aiConversacionId(domain.getAiConversacionId().value().toString())
            .tipoAccion(domain.getTipoAccion())
            .estado(domain.getEstado())
            .payloadJson(domain.getPayloadJson())
            .rationale(domain.getRationale())
            .version(domain.getVersion())
            .expiresAt(domain.getExpiresAt())
            .resultadoEntidadId(domain.getResultadoEntidadId())
            .errorReason(domain.getErrorReason())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .build();
    }

    /**
     * Maps a persistence entity to a domain {@link AiAccion}.
     * Used for find/load operations.
     */
    public static AiAccion toDomain(AiAccionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AiAccion.reconstitute(
            AiAccionId.from(UUID.fromString(entity.getId())),
            EmpresaId.from(UUID.fromString(entity.getEmpresaId())),
            UsuarioId.from(UUID.fromString(entity.getSolicitadaPor())),
            entity.getWaConversacionId(),
            entity.getWaMensajeId(),
            AiConversacionId.from(UUID.fromString(entity.getAiConversacionId())),
            entity.getTipoAccion(),
            entity.getPayloadJson(),
            entity.getRationale(),
            entity.getVersion(),
            entity.getExpiresAt(),
            entity.getResultadoEntidadId(),
            entity.getErrorReason(),
            entity.getEstado(),
            entity.getCreadoEn(),
            entity.getActualizadoEn()
        );
    }
}
