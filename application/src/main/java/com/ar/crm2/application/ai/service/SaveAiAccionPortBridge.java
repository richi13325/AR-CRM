package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.port.out.SaveAiAccionPort;
import com.ar.crm2.model.entity.ia.AiAccion;
import lombok.RequiredArgsConstructor;

/**
 * Thin bridge that exposes {@link SaveAiAccionPort} as a
 * service-style collaborator (so the {@code ConfirmarAccionService}
 * constructor signature stays clean).
 *
 * <p>Implemented as a Lombok-injected final field — no business logic.
 */
@RequiredArgsConstructor
public class SaveAiAccionPortBridge {

    private final SaveAiAccionPort port;

    public AiAccion save(AiAccion accion) {
        return port.save(accion);
    }
}