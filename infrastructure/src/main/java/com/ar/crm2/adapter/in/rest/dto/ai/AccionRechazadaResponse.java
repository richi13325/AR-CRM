package com.ar.crm2.adapter.in.rest.dto.ai;

import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response shape for {@code POST /api/ai/acciones/{id}/rechazar}.
 *
 * <p>Project the post-rejection {@link AiAccion} onto the wire — the
 * resulting {@link EstadoAccion#REJECTED} status plus the audit
 * linkage fields (id, empresaId, aiConversacionId) the client needs
 * to update its local cache.
 */
public record AccionRechazadaResponse(
    UUID id,
    UUID empresaId,
    UUID aiConversacionId,
    EstadoAccion estado,
    int version,
    LocalDateTime actualizadoEn
) {

    public static AccionRechazadaResponse fromDomain(AiAccion accion) {
        return new AccionRechazadaResponse(
                accion.getId().value(),
                accion.getEmpresaId().value(),
                accion.getAiConversacionId().value(),
                accion.getEstado(),
                accion.getVersion(),
                accion.getActualizadoEn()
        );
    }
}