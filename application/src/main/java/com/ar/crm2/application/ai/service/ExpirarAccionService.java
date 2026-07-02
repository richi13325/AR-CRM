package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ExpirarAccionCommand;
import com.ar.crm2.application.ai.port.in.ExpirarAccionUseCase;
import com.ar.crm2.application.ai.port.out.UpdateEstadoAccionPort;
import com.ar.crm2.exception.AccionStateTransitionException;
import com.ar.crm2.model.entity.ia.AiAccion;
import lombok.RequiredArgsConstructor;

/**
 * Application service that flips past-expiry PENDING proposals to
 * EXPIRED. Side-effect free at the CRM layer — only flips proposal
 * state.
 */
@RequiredArgsConstructor
public class ExpirarAccionService implements ExpirarAccionUseCase {

    private final UpdateEstadoAccionPort updateEstadoAccionPort;

    @Override
    public int expirar(ExpirarAccionCommand command) {
        var pendientes = updateEstadoAccionPort.findPendingExpired(command.maxPorLote());
        int expiradas = 0;
        for (AiAccion p : pendientes) {
            try {
                AiAccion expirada = p.expirar(command.ahora());
                updateEstadoAccionPort.save(expirada);
                expiradas++;
            } catch (AccionStateTransitionException ex) {
                // Already terminal — skip.
            }
        }
        return expiradas;
    }
}