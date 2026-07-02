package com.ar.crm2.application.ai.command;

import java.time.LocalDateTime;

/**
 * Command for the expiry sweep. Identifies a batch of proposals to
 * evaluate for expiry at the supplied instant.
 *
 * <p>Used by infrastructure-side scheduled jobs; the application use
 * case is the only path that flips PENDING → EXPIRED so the lifecycle
 * stays consistent.
 */
public record ExpirarAccionCommand(
    LocalDateTime ahora,
    int maxPorLote
) {

    public ExpirarAccionCommand {
        if (ahora == null) {
            throw new IllegalArgumentException("ahora is required");
        }
        if (maxPorLote <= 0 || maxPorLote > 1000) {
            throw new IllegalArgumentException("maxPorLote must be between 1 and 1000");
        }
    }
}