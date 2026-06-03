package com.ar.crm2.application.ficha.command;

import java.util.UUID;

/**
 * Command to move a Ficha to a different Columna.
 * Validates fichaId and targetColumnaId at construction time.
 */
public record MoverColumnaFichaCommand(
    UUID fichaId,
    UUID targetColumnaId
) {

    public MoverColumnaFichaCommand {
        if (fichaId == null) {
            throw new IllegalArgumentException("fichaId is required");
        }
        if (targetColumnaId == null) {
            throw new IllegalArgumentException("targetColumnaId is required");
        }
    }
}
