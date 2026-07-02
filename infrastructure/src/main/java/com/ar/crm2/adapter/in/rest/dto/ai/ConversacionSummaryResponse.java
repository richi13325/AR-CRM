package com.ar.crm2.adapter.in.rest.dto.ai;

import com.ar.crm2.model.entity.ia.AiConversacion;

import java.util.UUID;

/**
 * Surface-level projection of an {@link AiConversacion} for the
 * list endpoints. Only the fields the UI needs to render a list view
 * are exposed.
 */
public record ConversacionSummaryResponse(
    UUID id,
    UUID empresaId,
    UUID actorUsuarioId,
    String waConversacionId,
    boolean archivada,
    String creadoEn,
    String actualizadoEn
) {

    public static ConversacionSummaryResponse fromDomain(AiConversacion conv) {
        return new ConversacionSummaryResponse(
                conv.getId().value(),
                conv.getEmpresaId().value(),
                conv.getActorUsuarioId().value(),
                conv.getWaConversacionId(),
                conv.isArchivada(),
                conv.getCreadoEn().toString(),
                conv.getActualizadoEn().toString()
        );
    }
}