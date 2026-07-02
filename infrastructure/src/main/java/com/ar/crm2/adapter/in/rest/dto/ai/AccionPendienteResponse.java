package com.ar.crm2.adapter.in.rest.dto.ai;

import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Wire-shape projection of a PENDING AI action proposal surfaced
 * through {@code GET /api/ai/acciones}.
 *
 * <p>Exposes only the fields a UI inbox needs to render and dispatch
 * confirmation/rejection: identity, state, type, rationale, expiry,
 * and the originating AI conversation id. The full payload is
 * fetched on demand by the confirm/reject endpoints via the
 * {@code ConfirmarAccionUseCase} / {@code RechazarAccionUseCase}
 * entry points.
 */
public record AccionPendienteResponse(
    UUID id,
    UUID empresaId,
    UUID aiConversacionId,
    String tipoAccion,
    String rationale,
    int version,
    LocalDateTime expiraEn,
    EstadoAccion estado,
    LocalDateTime creadoEn
) {

    public static AccionPendienteResponse fromDomain(AiAccion accion) {
        return new AccionPendienteResponse(
            accion.getId().value(),
            accion.getEmpresaId().value(),
            accion.getAiConversacionId() != null ? accion.getAiConversacionId().value() : null,
            accion.getTipoAccion(),
            accion.getRationale(),
            accion.getVersion(),
            accion.getExpiresAt(),
            accion.getEstado(),
            accion.getCreadoEn()
        );
    }
}
