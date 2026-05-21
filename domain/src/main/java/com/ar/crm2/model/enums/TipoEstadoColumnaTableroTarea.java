package com.ar.crm2.model.enums;

/**
 * Semantic state for a board column belonging to a task board.
 * Used exclusively when {@link TipoTablero} is {@code TAREAS}.
 */
public enum TipoEstadoColumnaTableroTarea {
    PENDIENTE,
    EN_CURSO,
    FINALIZADA
}