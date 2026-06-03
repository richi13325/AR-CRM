package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.exception.DomainException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Domain entity for Ficha (Kanban card).
 *
 * Identity: FichaId.
 * Equality: by id only.
 * Constructor is private; use static factory methods create() and reconstitute().
 *
 * Represents only the Kanban card position/location, not business metadata.
 * Business metadata (responsableId, creadoPor, creadoEn) belongs to Tarea/Trato.
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

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Ficha.
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
        return new Ficha(FichaId.create(), columnaId, tipoFicha, tratoId, tareaId, now);
    }

    /**
     * Reconstitutes an existing Ficha from persistence.
     * Validation:
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
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(columnaId, "columnaId");
        DomainAssert.notNull(tipoFicha, "tipoFicha");
        DomainAssert.notNull(actualizadoEn, "actualizadoEn");

        validarConsistenciaTipoFicha(tipoFicha, tratoId, tareaId);

        return new Ficha(id, columnaId, tipoFicha, tratoId, tareaId, actualizadoEn);
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
            Instant.now()
        );
    }
}