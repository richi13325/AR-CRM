package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ExpirarAccionCommand;

/**
 * Inbound use case: evaluate pending proposals and mark the ones
 * past their expiry as EXPIRED. Side-effect free at the application
 * boundary — only flips proposal state.
 */
public interface ExpirarAccionUseCase {

    /**
     * Processes up to {@code command.maxPorLote()} pending proposals.
     *
     * @return number of proposals that were marked EXPIRED
     */
    int expirar(ExpirarAccionCommand command);
}