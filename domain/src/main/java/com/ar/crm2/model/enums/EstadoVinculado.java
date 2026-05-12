package com.ar.crm2.model.enums;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.shared.DomainAssert;
import lombok.RequiredArgsConstructor;

/**
 * Estado vinculable a una Columna.
 * Cada constant lleva su TipoTablero owner; el dominio asegura que
 * el estado asignado pertenezca al mismo tablero.
 */
@RequiredArgsConstructor
public enum EstadoVinculado {

    // ── TAREAS ────────────────────────────────────────────────────
    PENDIENTE(TipoTablero.TAREAS),
    EN_PROCESO(TipoTablero.TAREAS),
    ESPERANDO_RESPUESTA(TipoTablero.TAREAS),
    COMPLETADA(TipoTablero.TAREAS),
    CANCELADA(TipoTablero.TAREAS),

    // ── TRATOS ────────────────────────────────────────────────────
    NUEVO(TipoTablero.TRATOS),
    CONTACTADO(TipoTablero.TRATOS),
    PROPUESTA_ENVIADA(TipoTablero.TRATOS),
    NEGOCIACION(TipoTablero.TRATOS),
    GANADO(TipoTablero.TRATOS),
    PERDIDO(TipoTablero.TRATOS);

    private final TipoTablero tipoTablero;

    public void assertCompatibleCon(TipoTablero tipoTablero) {
        DomainAssert.notNull(tipoTablero, "tipoTablero is mandatory");
        DomainAssert.sameAs(this.tipoTablero, tipoTablero,
            "Estado '" + this.name() + "' no pertenece a TipoTablero '" + tipoTablero.name() + "'");
    }
}
