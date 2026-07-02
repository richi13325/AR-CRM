package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiAccion;

import java.util.List;

/**
 * Outbound port for the expiry sweep — finds proposals past their
 * {@code expiresAt} that are still in PENDING state.
 */
public interface UpdateEstadoAccionPort {

    List<AiAccion> findPendingExpired(int limite);

    AiAccion save(AiAccion accion);
}