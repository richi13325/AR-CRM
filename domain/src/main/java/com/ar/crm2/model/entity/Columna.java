package com.ar.crm2.model.entity;

import com.ar.crm2.exception.ColumnaEnUsoNoPuedeEliminarseException;
import com.ar.crm2.exception.NombreColumnaYaExisteException;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.SuperUsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;

/**
 * Domain entity for Columna.
 *
 * <p>Identity: ColumnaId.
 * <p>Equality: by id only.
 * <p>Constructor is private; use static factory methods create() and reconstitute().
 *
 * <p>Columna is a pure catalog entity — board-specific column instances are
 * represented by {@link ColumnaTablero}, not by embedding board context here.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Columna {

    @EqualsAndHashCode.Include
    private final ColumnaId id;

    private final String columnanombre;
    private final String color;
    private final TipoTablero tipoTablero;
    private final TipoColumna tipoColumna;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new Columna.
     *
     * <p>Authorization rules:
     * <ul>
     *   <li>Si tipoColumna == PREDETERMINADA: superUsuarioId es obligatorio.</li>
     *   <li>Si tipoColumna == PERSONALIZADA: superUsuarioId es opcional (nullable).</li>
     * </ul>
     *
     * <p>Validation:
     * <ul>
     *   <li>nombre 1-80 chars</li>
     *   <li>tipoTablero mandatory</li>
     *   <li>tipoColumna mandatory</li>
     * </ul>
     *
     * <p>Defaults:
     * <ul>
     *   <li>color null -> "#FFFFFF"</li>
     * </ul>
     *
     * @param superUsuarioId optional for PERSONALIZADA, mandatory for PREDETERMINADA
     * @param nombre          mandatory - column name, 1-80 chars
     * @param tipoTablero     mandatory - type of board (TAREAS or TRATOS)
     * @param tipoColumna     mandatory - type of column (PREDETERMINADA or PERSONALIZADA)
     * @param color           optional - defaults to #FFFFFF
     * @param existeOtraColumnaConMismoNombre guard for duplicate name in catalog scope
     */
    public static Columna create(
        SuperUsuarioId superUsuarioId,
        String nombre,
        TipoTablero tipoTablero,
        TipoColumna tipoColumna,
        String color,
        boolean existeOtraColumnaConMismoNombre
    ) {
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        DomainAssert.notNull(tipoColumna, "tipoColumna");
        String normalizedNombre = validarNombre(nombre, existeOtraColumnaConMismoNombre);

        if (TipoColumna.PREDETERMINADA.equals(tipoColumna)) {
            DomainAssert.notNull(superUsuarioId, "superUsuarioId");
        }

        String normalizedColor = (color == null) ? "#FFFFFF" : color;
        DomainAssert.lengthBetween(normalizedColor, "color", 1, 7);

        return new Columna(
            ColumnaId.create(),
            normalizedNombre,
            normalizedColor,
            tipoTablero,
            tipoColumna
        );
    }

    /**
     * Reconstitutes an existing Columna from persistence.
     *
     * @param id                                  the column id
     * @param nombre                              the column name
     * @param color                               the column color
     * @param tipoTablero                         the board type (TAREAS or TRATOS)
     * @param tipoColumna                          the column type (PREDETERMINADA or PERSONALIZADA)
     * @param existeOtraColumnaConMismoNombre    guard for duplicate name in catalog scope
     */
    public static Columna reconstitute(
        ColumnaId id,
        String nombre,
        String color,
        TipoTablero tipoTablero,
        TipoColumna tipoColumna,
        boolean existeOtraColumnaConMismoNombre
    ) {
        String normalizedColor = (color == null) ? "#FFFFFF" : color;

        return new Columna(
            DomainAssert.notNull(id, "id"),
            validarNombre(nombre, existeOtraColumnaConMismoNombre),
            DomainAssert.lengthBetween(normalizedColor, "color", 1, 7),
            DomainAssert.notNull(tipoTablero, "tipoTablero"),
            DomainAssert.notNull(tipoColumna, "tipoColumna")
        );
    }

    // ── Name validation (centralized) ─────────────────────────────────

    /**
     * Centralizes name validation for create, reconstitute, and renombrar.
     *
     * <p>Applies in order:
     * <ol>
     *   <li>Length check 1-80 via {@link DomainAssert#lengthBetween}</li>
     *   <li>Duplicate guard — throws {@link NombreColumnaYaExisteException} if flag is true</li>
     * </ol>
     *
     * @return the normalized (already-trimmed) name returned by DomainAssert.lengthBetween
     * @throws NombreColumnaYaExisteException if existeOtraColumnaConMismoNombre is true
     */
    private static String validarNombre(String nombre, boolean existeOtraColumnaConMismoNombre) {
        String normalized = DomainAssert.lengthBetween(nombre, "nombre", 1, 80);
        if (existeOtraColumnaConMismoNombre) {
            throw new NombreColumnaYaExisteException();
        }
        return normalized;
    }

    // ── Domain helpers ─────────────────────────────────────────────

    /**
     * Renames this column, returning a new immutable Columna with the new name.
     *
     * <p>All other fields (id, color, tipoTablero, tipoColumna) are preserved
     * exactly as they are — only columnanombre changes, after validation.
     *
     * @param nuevoNombre                       the desired new name, 1-80 chars
     * @param existeOtraColumnaConMismoNombre   true to force a duplicate-name violation
     * @return a new Columna instance with the validated new name
     * @throws NombreColumnaYaExisteException if the duplicate flag is true
     */
    public Columna renombrar(String nuevoNombre, boolean existeOtraColumnaConMismoNombre) {
        String normalizedNombre = validarNombre(nuevoNombre, existeOtraColumnaConMismoNombre);
        return new Columna(
            this.id,
            normalizedNombre,
            this.color,
            this.tipoTablero,
            this.tipoColumna
        );
    }

    public boolean esPredeterminada() {
        return TipoColumna.PREDETERMINADA.equals(tipoColumna);
    }

    public boolean esPersonalizada() {
        return TipoColumna.PERSONALIZADA.equals(tipoColumna);
    }

    // ── Deletion eligibility ────────────────────────────────────────

    /**
     * Guards the definitive deletion of this column.
     *
     * <p>The application layer determines whether the column is in use by any board
     * and passes the result as {@code estaEnUsoEnTablero}. The domain layer throws
     * if the business rule is violated, otherwise returns normally — no state is
     * mutated and no repository is involved.
     *
     * @param estaEnUsoEnTablero true if the column is referenced by at least one Tablero
     * @throws ColumnaEnUsoNoPuedeEliminarseException if the column is in use
     */
    public void eliminar(boolean estaEnUsoEnTablero) {
        if (estaEnUsoEnTablero) {
            throw new ColumnaEnUsoNoPuedeEliminarseException();
        }
    }
}