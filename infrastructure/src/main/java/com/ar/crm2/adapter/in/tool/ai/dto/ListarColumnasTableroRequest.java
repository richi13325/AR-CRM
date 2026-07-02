package com.ar.crm2.adapter.in.tool.ai.dto;

import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Infrastructure DTO passed by the model to the
 * {@code listarColumnasTablero} AI tool.
 *
 * <p>The model supplies the board type ({@code TAREAS} or
 * {@code TRATOS}); the request DTO converts the string to the
 * {@link com.ar.crm2.model.enums.TipoTablero} enum via the
 * factory so a typo surfaces as an {@code IllegalArgumentException}
 * to the model rather than a silent empty list.
 */
public record ListarColumnasTableroRequest(
    @ToolParam(description = "Board type whose catalog columns are being listed. "
        + "Must be one of: TAREAS, TRATOS.",
        required = true)
    String tipoTablero
) {

    public ListarColumnasTableroRequest {
        if (tipoTablero == null || tipoTablero.isBlank()) {
            throw new IllegalArgumentException("tipoTablero is required");
        }
    }

    /**
     * Returns the request's board type as the domain enum.
     *
     * @throws IllegalArgumentException when {@code tipoTablero} does
     *         not match any {@link com.ar.crm2.model.enums.TipoTablero}
     *         constant (propagates to the model as a clear error).
     */
    public com.ar.crm2.model.enums.TipoTablero tipoTableroAsEnum() {
        return com.ar.crm2.model.enums.TipoTablero.valueOf(tipoTablero);
    }
}