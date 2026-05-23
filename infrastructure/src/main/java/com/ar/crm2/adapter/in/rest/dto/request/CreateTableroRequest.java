package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoTablero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for creating a new Tablero.
 * Required fields: nombre, descripcion, tipoTablero.
 * Timestamps (creadoEn) and id are never accepted from the client.
 * The 4 default columns are synthesized by the application service, not provided by the client.
 *
 * <p><strong>Note:</strong> As of the auth foundation pilot, {@code superUsuarioId}
 * is IGNORED for the {@code /api/tableros/create} endpoint — the ID is now derived
 * from the authenticated actor context (JWT token). The field remains in the DTO
 * for backwards compatibility with existing API contracts but has no effect.
 */
public record CreateTableroRequest(
    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 100, message = "nombre must be 1-100 characters")
    String nombre,

    @NotBlank(message = "descripcion is required")
    String descripcion,

    @NotNull(message = "tipoTablero is required")
    TipoTablero tipoTablero,

    // Deprecated: superUsuarioId is now ignored — derived from JWT actor context (auth foundation pilot).
    // Field retained for API backwards compatibility.
    UUID superUsuarioId,

    boolean columnasPredeterminadas
) {}