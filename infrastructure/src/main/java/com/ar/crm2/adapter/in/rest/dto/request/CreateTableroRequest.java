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
 * <h2>Legacy contract (transitional)</h2>
 * <p>This DTO carries two legacy fields whose removal is pending until the
 * replacement contract is fully stable:
 * <ul>
 *   <li>{@code superUsuarioId} — IGNORED on the server. The actor id is
 *       derived from the authenticated {@code ActorContext} (JWT token).
 *       The field is retained for backwards API compatibility.</li>
 *   <li>{@code columnasPredeterminadas} — the create endpoint always
 *       assembles the 4 default columns (the board shape is a domain
 *       rule owned by {@code Tablero.requiredDefaultColumns(TipoTablero)}).
 *       The flag is retained for backwards API compatibility and may be
 *       removed once the request contract clearly maps WIP to
 *       {@code ColumnaTablero} assignments.</li>
 * </ul>
 *
 * <h2>Replacement contract (where WIP lives)</h2>
 * <p>WIP limits belong to the {@code ColumnaTablero} contextual wrapper
 * (i.e., to a catalog {@code Columna} when it is assigned to a specific
 * {@code Tablero}). WIP is contextual to the board, not a property of the
 * catalog column itself. The new request shape for assigning a column
 * to a board is {@link AsignarColumnaRequest}, which carries the WIP
 * limit directly. {@code CreateTableroRequest} does not (yet) accept
 * per-column WIP values; the application service applies the canonical
 * defaults from {@code Tablero.requiredDefaultColumns(TipoTablero)}.
 *
 * <p>Per the SDD change {@code tablero-clean-architecture-audit}, the
 * legacy fields are preserved until a future change retires them.
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
    // Field retained for API backwards compatibility until the replacement contract is stable.
    UUID superUsuarioId,

    // Deprecated: columnasPredeterminadas is always true at the server (the domain assembles
    // 4 default columns for every board). Retained for backwards API compatibility.
    boolean columnasPredeterminadas
) {}