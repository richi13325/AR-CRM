package com.ar.crm2.application.ai.port.in.result;

import com.ar.crm2.model.enums.EstadoAccion;

/**
 * Result of confirming an AI action proposal.
 *
 * <p>Either the proposal transitioned to {@link EstadoAccion#EXECUTED}
 * (with the resulting entity id) or to {@link EstadoAccion#FAILED}
 * (with an error reason). The application service guarantees the
 * proposal state is updated even on mutation failure so the lifecycle
 * stays observable.
 */
public record ResultadoEjecucionAccion(
    EstadoAccion estado,
    String resultadoEntidadId,
    String errorReason,
    int nuevaVersion
) {

    public ResultadoEjecucionAccion {
        if (estado == null) {
            throw new IllegalArgumentException("estado is required");
        }
        if (nuevaVersion <= 0) {
            throw new IllegalArgumentException("nuevaVersion must be positive");
        }
    }

    public static ResultadoEjecucionAccion ejecutada(String resultadoEntidadId, int nuevaVersion) {
        return new ResultadoEjecucionAccion(
                EstadoAccion.EXECUTED, resultadoEntidadId, null, nuevaVersion
        );
    }

    public static ResultadoEjecucionAccion fallida(String errorReason, int nuevaVersion) {
        return new ResultadoEjecucionAccion(
                EstadoAccion.FAILED, null, errorReason, nuevaVersion
        );
    }
}
