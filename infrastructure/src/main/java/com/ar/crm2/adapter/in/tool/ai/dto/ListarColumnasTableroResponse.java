package com.ar.crm2.adapter.in.tool.ai.dto;

/**
 * Infrastructure DTO returned by the {@code listarColumnasTablero}
 * AI tool — one record per catalog column for the requested board
 * type.
 *
 * <p>The shape is intentionally minimal: the model needs id / name /
 * color / type to pick a valid target column before proposing
 * {@code MOVE_KANBAN_FICHA}.
 */
public record ListarColumnasTableroResponse(
    String id,
    String nombre,
    String color,
    String tipoTablero,
    String tipoColumna
) {
}