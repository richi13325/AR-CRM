package com.ar.crm2.application.tablero.command;

import com.ar.crm2.model.enums.TipoTablero;

import java.util.UUID;

/**
 * Command to create a new Tablero.
 * Required fields validated at construction time.
 *
 * <p><strong>Authorization:</strong> any authenticated user may create a Tablero.
 * The application layer is responsible for resolving the actor from the
 * authenticated context; the domain does NOT require a {@code SuperUsuarioId}
 * for creation.
 *
 * <p>Field semantics:
 * <ul>
 *   <li>{@code actorId} — orchestration context only. The mapper populates it
 *       from the authenticated actor (superusuario or normal usuario). The
 *       domain ignores it; it is kept on the command for legacy traceability
 *       and audit until the replacement contract fully retires the field.</li>
 *   <li>{@code columnasPredeterminadas} — legacy flag preserved for
 *       backwards API compatibility. The application service always
 *       assembles the 4 default columns regardless of this flag because
 *       the board shape is a domain rule.</li>
 * </ul>
 */
public record CreateTableroCommand(
    String nombre,
    String descripcion,
    TipoTablero tipoTablero,
    boolean columnasPredeterminadas,
    UUID actorId
) {

    public CreateTableroCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("descripcion is required");
        }
        if (tipoTablero == null) {
            throw new IllegalArgumentException("tipoTablero is required");
        }
        if (actorId == null) {
            throw new IllegalArgumentException("actorId is required");
        }
    }
}
