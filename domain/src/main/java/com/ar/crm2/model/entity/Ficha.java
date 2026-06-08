package com.ar.crm2.model.entity;

import com.ar.crm2.exception.EtiquetaTypeMismatchException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Domain entity for Ficha (Kanban card).
 *
 * <p>Identity: FichaId.
 * Equality: by id only.
 * Constructor is private; use static factory methods create() and reconstitute().
 *
 * <p>Represents only the Kanban card position/location, not business metadata.
 * Business metadata (responsableId, creadoPor, creadoEn) belongs to Tarea/Trato.
 *
 * <p>Owns a list of {@link FichaEtiqueta} relations following the same
 * contextual-wrapper pattern as {@link Tablero} owning {@link ColumnaTablero}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ficha {

    @EqualsAndHashCode.Include
    private final FichaId id;

    private final ColumnaId columnaId;
    private final TipoFicha tipoFicha;
    private final TratoId tratoId;
    private final TareaId tareaId;
    private final Instant actualizadoEn;
    private final List<FichaEtiqueta> etiquetas;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Ficha with no etiquetas.
     * Validation:
     * - columnaId mandatory
     * - tipoFicha mandatory
     * - TAREA requires tareaId != null and tratoId == null
     * - TRATO requires tratoId != null and tareaId == null
     *
     * @param columnaId     mandatory
     * @param tipoFicha      mandatory
     * @param tratoId        nullable (required if tipoFicha == TRATO)
     * @param tareaId        nullable (required if tipoFicha == TAREA)
     */
    public static Ficha create(
        ColumnaId columnaId,
        TipoFicha tipoFicha,
        TratoId tratoId,
        TareaId tareaId
    ) {
        DomainAssert.notNull(columnaId, "columnaId");
        DomainAssert.notNull(tipoFicha, "tipoFicha");

        validarConsistenciaTipoFicha(tipoFicha, tratoId, tareaId);

        Instant now = Instant.now();
        return new Ficha(FichaId.create(), columnaId, tipoFicha, tratoId, tareaId, now, List.of());
    }

    /**
     * Reconstitutes an existing Ficha from persistence with no etiquetas.
     * Backward-compatible overload that treats the etiqueta list as empty.
     *
     * <p>Validation:
     * - TAREA requires tareaId != null and tratoId == null
     * - TRATO requires tratoId != null and tareaId == null
     *
     * @param id             mandatory
     * @param columnaId      mandatory
     * @param tipoFicha      mandatory
     * @param tratoId        nullable (required if tipoFicha == TRATO)
     * @param tareaId        nullable (required if tipoFicha == TAREA)
     * @param actualizadoEn  mandatory
     */
    public static Ficha reconstitute(
        FichaId id,
        ColumnaId columnaId,
        TipoFicha tipoFicha,
        TratoId tratoId,
        TareaId tareaId,
        Instant actualizadoEn
    ) {
        return reconstitute(id, columnaId, tipoFicha, tratoId, tareaId, actualizadoEn, List.of());
    }

    /**
     * Reconstitutes an existing Ficha from persistence with an explicit etiquetas list.
     *
     * <p>Validation:
     * - TAREA requires tareaId != null and tratoId == null
     * - TRATO requires tratoId != null and tareaId == null
     * - etiquetas list, when non-empty, must have tipos matching the Ficha's tipoFicha
     *   and no duplicate EtiquetaId values.
     *
     * @param id             mandatory
     * @param columnaId      mandatory
     * @param tipoFicha      mandatory
     * @param tratoId        nullable (required if tipoFicha == TRATO)
     * @param tareaId        nullable (required if tipoFicha == TAREA)
     * @param actualizadoEn  mandatory
     * @param etiquetas      may be null (treated as empty); entries are validated
     */
    public static Ficha reconstitute(
        FichaId id,
        ColumnaId columnaId,
        TipoFicha tipoFicha,
        TratoId tratoId,
        TareaId tareaId,
        Instant actualizadoEn,
        List<FichaEtiqueta> etiquetas
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(columnaId, "columnaId");
        DomainAssert.notNull(tipoFicha, "tipoFicha");
        DomainAssert.notNull(actualizadoEn, "actualizadoEn");

        validarConsistenciaTipoFicha(tipoFicha, tratoId, tareaId);
        List<FichaEtiqueta> resolvedEtiquetas = validarEtiquetas(etiquetas, tipoFicha);

        return new Ficha(id, columnaId, tipoFicha, tratoId, tareaId, actualizadoEn, resolvedEtiquetas);
    }

    // ── Domain Invariants ───────────────────────────────────────────

    /**
     * Enforces TipoFicha ↔ related-id consistency.
     * <ul>
     *   <li>TAREA: tareaId required, tratoId forbidden</li>
     *   <li>TRATO: tratoId required, tareaId forbidden</li>
     * </ul>
     */
    private static void validarConsistenciaTipoFicha(TipoFicha tipoFicha, TratoId tratoId, TareaId tareaId) {
        if (tipoFicha == TipoFicha.TAREA) {
            DomainAssert.notNull(tareaId, "tareaId");
            if (tratoId != null) {
                throw new InvariantViolationException(
                    "Una Ficha de tipo TAREA no puede tener tratoId.");
            }
        } else if (tipoFicha == TipoFicha.TRATO) {
            DomainAssert.notNull(tratoId, "tratoId");
            if (tareaId != null) {
                throw new InvariantViolationException(
                    "Una Ficha de tipo TRATO no puede tener tareaId.");
            }
        }
    }

    /**
     * Validates the etiquetas list: rejects null entries, duplicate EtiquetaId values,
     * and any entry whose TipoEtiqueta does not match the Ficha's TipoFicha.
     */
    private static List<FichaEtiqueta> validarEtiquetas(
        List<FichaEtiqueta> etiquetas,
        TipoFicha tipoFicha
    ) {
        if (etiquetas == null || etiquetas.isEmpty()) {
            return List.of();
        }
        TipoEtiqueta tipoEsperado = TipoEtiqueta.fromFicha(tipoFicha);

        Set<com.ar.crm2.model.vo.EtiquetaId> seenIds = new HashSet<>();
        List<FichaEtiqueta> resolved = new ArrayList<>(etiquetas.size());
        for (int i = 0; i < etiquetas.size(); i++) {
            FichaEtiqueta fe = etiquetas.get(i);
            if (fe == null) {
                throw new InvariantViolationException(
                    "La etiqueta en posición " + i + " no puede ser null.");
            }
            if (!seenIds.add(fe.getEtiquetaId())) {
                throw new InvariantViolationException(
                    "La lista de etiquetas contiene EtiquetaId duplicados.");
            }
            if (!fe.getTipoEtiqueta().equals(tipoEsperado)) {
                throw new EtiquetaTypeMismatchException();
            }
            resolved.add(fe);
        }
        return List.copyOf(resolved);
    }

    // ── Column Movement ──────────────────────────────────────────────

    /**
     * Moves this Ficha to a new column.
     * <p>
     * Immutable operation: returns the same instance when target column equals
     * the current column (idempotent no-op), or a new Ficha with the new
     * columnaId and updated actualizadoEn timestamp when different.
     *
     * @param nuevaColumnaId mandatory target column; must not be null
     * @return this instance if columnaId already equals nuevaColumnaId;
     *         otherwise a new Ficha with nuevaColumnaId and actualizadoEn set to now
     * @throws InvariantViolationException if nuevaColumnaId is null
     */
    public Ficha moverAColumna(ColumnaId nuevaColumnaId) {
        DomainAssert.notNull(nuevaColumnaId, "nuevaColumnaId");
        if (nuevaColumnaId.equals(this.columnaId)) {
            return this;
        }
        return new Ficha(
            id,
            nuevaColumnaId,
            tipoFicha,
            tratoId,
            tareaId,
            Instant.now(),
            this.etiquetas
        );
    }

    // ── Etiquetas management ─────────────────────────────────────────

    /**
     * Returns a new Ficha with the given etiquetas list, preserving identity and other fields.
     *
     * <p>Rules:
     * - etiquetas may be null (treated as empty).
     * - etiquetas entries must not be null.
     * - EtiquetaId must be unique within the list.
     * - Every entry's TipoEtiqueta must match this Ficha's TipoFicha.
     *
     * @param etiquetas the new list of etiqueta relations
     * @return a new Ficha with the updated etiquetas
     * @throws InvariantViolationException if any entry is null or EtiquetaId is duplicated
     * @throws EtiquetaTypeMismatchException if any entry's TipoEtiqueta does not match the Ficha
     */
    public Ficha withEtiquetas(List<FichaEtiqueta> etiquetas) {
        List<FichaEtiqueta> resolved = validarEtiquetas(etiquetas, this.tipoFicha);
        return new Ficha(
            this.id,
            this.columnaId,
            this.tipoFicha,
            this.tratoId,
            this.tareaId,
            this.actualizadoEn,
            resolved
        );
    }
}
