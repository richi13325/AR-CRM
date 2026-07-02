package com.ar.crm2.adapter.in.rest.dto.ai;

import com.ar.crm2.application.ai.port.in.result.ResultadoEjecucionAccion;
import com.ar.crm2.model.enums.EstadoAccion;

/**
 * Response shape for {@code POST /api/ai/acciones/{id}/confirmar}.
 *
 * <p>Surface-level projection of {@link ResultadoEjecucionAccion} — the
 * client receives the resulting {@code estado}
 * ({@link EstadoAccion#EXECUTED} or {@link EstadoAccion#FAILED}), the
 * id of the entity created by the underlying mutation use case (null on
 * failure), the error reason (null on success), and the new optimistic
 * lock version so the UI can issue follow-up calls.
 */
public record AccionEjecutadaResponse(
    EstadoAccion estado,
    String resultadoEntidadId,
    String errorReason,
    int nuevaVersion
) {

    public static AccionEjecutadaResponse fromDomain(ResultadoEjecucionAccion result) {
        return new AccionEjecutadaResponse(
                result.estado(),
                result.resultadoEntidadId(),
                result.errorReason(),
                result.nuevaVersion()
        );
    }
}