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

import java.util.List;

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
     * <p>Authorization rules are intentionally NOT enforced in the domain
     * entity. Whether a caller may create a PREDETERMINADA or PERSONALIZADA
     * catalog column is an application/security concern. The legacy
     * {@code superUsuarioId} parameter is accepted for caller compatibility
     * but is not part of this entity's invariants.
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
     * @param superUsuarioId legacy caller context; ignored by domain invariants
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

        String normalizedColor = (color == null) ? "#FFFFFF" : color;
        DomainAssert.lengthBetween(normalizedColor, "color", 1, 7);

        return new Columna(
            ColumnaId.create(),
            normalizedNombre,
            normalizedColor.trim(),
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
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        DomainAssert.notNull(tipoColumna, "tipoColumna");
        DomainAssert.lengthBetween(normalizedColor, "color", 1, 7);

        return new Columna(
            id,
            validarNombre(nombre, existeOtraColumnaConMismoNombre),
            normalizedColor.trim(),
            tipoTablero,
            tipoColumna
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
     * @return the normalized (trimmed) name
     * @throws NombreColumnaYaExisteException if existeOtraColumnaConMismoNombre is true
     */
    private static String validarNombre(String nombre, boolean existeOtraColumnaConMismoNombre) {
        DomainAssert.lengthBetween(nombre, "nombre", 1, 80);
        if (existeOtraColumnaConMismoNombre) {
            throw new NombreColumnaYaExisteException();
        }
        return nombre.trim();
    }

    // ── Catalog naming helpers (domain-owned) ─────────────────────────

    /**
     * Normalizes a column name for duplicate-scope evaluation. Validates the
     * length range 1-80 via {@link DomainAssert#lengthBetween} and returns
     * the trimmed name.
     *
     * @param nombre the raw column name
     * @return the normalized (trimmed, length-checked) name
     * @throws com.ar.crm2.exception.InvariantViolationException if nombre is null, blank, or out of range
     */
    public static String normalize(String nombre) {
        DomainAssert.lengthBetween(nombre, "nombre", 1, 80);
        return nombre.trim();
    }

    /**
     * Evaluates whether a new column with the given (tipoTablero, normalized nombre)
     * would collide with any existing catalog column for the same {@link TipoTablero}.
     *
     * <p>Used by application services that previously delegated to the now-removed
     * {@code ColumnaNamePolicy} helper. The duplicate check is a domain responsibility
     * because the catalog identity (tipoTablero + normalized name) lives here.
     *
     * @param columnas     the existing catalog (used to scan for collisions)
     * @param tipoTablero  the board type the new column belongs to
     * @param nombre       the proposed name (will be normalized internally)
     * @return true if any existing column for the same tipoTablero has the same normalized name
     */
    public static boolean hasDuplicateForCreate(
        List<Columna> columnas,
        TipoTablero tipoTablero,
        String nombre
    ) {
        DomainAssert.notNull(columnas, "columnas");
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        String normalizedNombre = normalize(nombre);
        return columnas.stream()
            .filter(columna -> columna.getTipoTablero() == tipoTablero)
            .anyMatch(columna -> normalize(columna.getColumnanombre()).equals(normalizedNombre));
    }

    /**
     * Evaluates whether editing the given column to the new (tipoTablero, normalized nombre)
     * would collide with any OTHER existing catalog column for the same {@link TipoTablero}.
     *
     * <p>The column identified by {@code self} is excluded from the comparison so that
     * editing a column to its own current name does not report a false duplicate.
     *
     * @param columnas     the existing catalog
     * @param self         the id of the column being edited (excluded from comparison)
     * @param tipoTablero  the board type of the edited column
     * @param nombre       the proposed new name
     * @return true if any OTHER existing column for the same tipoTablero has the same normalized name
     */
    public static boolean hasDuplicateForEdit(
        List<Columna> columnas,
        ColumnaId self,
        TipoTablero tipoTablero,
        String nombre
    ) {
        DomainAssert.notNull(columnas, "columnas");
        DomainAssert.notNull(self, "self");
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        String normalizedNombre = normalize(nombre);
        return columnas.stream()
            .filter(columna -> columna.getTipoTablero() == tipoTablero)
            .filter(columna -> !columna.getId().equals(self))
            .anyMatch(columna -> normalize(columna.getColumnanombre()).equals(normalizedNombre));
    }

    /**
     * Determines whether this column matches the requested default catalog slot.
     *
     * <p>A column matches a default catalog slot when it:
     * <ul>
     *   <li>Belongs to the given {@link TipoTablero}</li>
     *   <li>Has {@link TipoColumna#PREDETERMINADA} type</li>
     *   <li>Has a name that normalizes to the requested default name</li>
     * </ul>
     *
     * <p>Used by the application service to reuse existing PREDETERMINADA catalog
     * entries when assembling a new board's default column shape. The default
     * catalog matching semantics is a domain responsibility because the column
     * identity rules live here.
     *
     * @param tipoTablero  the board type being assembled
     * @param nombre       the default column name being looked up (will be normalized)
     * @return true if this column matches the requested default catalog slot
     */
    public boolean matchesDefaultCatalog(TipoTablero tipoTablero, String nombre) {
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        String normalizedNombre = normalize(nombre);
        return this.tipoTablero == tipoTablero
            && this.tipoColumna == TipoColumna.PREDETERMINADA
            && normalize(this.columnanombre).equals(normalizedNombre);
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
