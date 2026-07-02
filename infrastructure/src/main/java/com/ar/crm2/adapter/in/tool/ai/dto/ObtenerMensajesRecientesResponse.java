package com.ar.crm2.adapter.in.tool.ai.dto;

import java.time.LocalDateTime;

/**
 * Infrastructure DTO returned by the {@code obtenerMensajesRecientes}
 * AI tool.
 *
 * <p>One record per message so Spring AI serializes a JSON array the
 * model can read directly. The shape is intentionally minimal — the
 * model needs id / direction / type / content / timestamp to cite
 * messages back to the user; media URLs are passed through when
 * present.
 */
public record ObtenerMensajesRecientesResponse(
    String id,
    String direccion,
    String tipo,
    String contenido,
    String mediaUrl,
    LocalDateTime creadoEn
) {
}