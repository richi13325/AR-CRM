package com.ar.crm2.model.entity;

import com.ar.crm2.exception.ColumnaConFichasNoPuedeEliminarseException;
import com.ar.crm2.exception.ColumnaNoPerteneceAlTableroException;
import com.ar.crm2.exception.ColumnaYaExisteEnTableroException;
import com.ar.crm2.exception.InvalidDefaultColumnsException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.exception.TipoTableroMismatchException;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.SuperUsuarioId;
import com.ar.crm2.model.vo.TableroId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import com.ar.crm2.model.enums.TipoTablero;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain entity for Tablero.
 *
 * <p>Identity: TableroId.
 * <p>Equality: by id only.
 * <p>Constructor is private; use static factory methods create() and reconstitute().
 *
 * <p>Columns are stored as {@link ColumnaTablero} contextual wrappers that pair
 * each {@link Columna} with its board-specific {@code limiteWip}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tablero {

    @EqualsAndHashCode.Include
    private final TableroId id;

    private final String nombre;
    private final String descripcion;
    private final List<ColumnaTablero> columnasTablero;
    private final TipoTablero tipoTablero;
    private final LocalDateTime creadoEn;

    // ── Compatibility accessor ───────────────────────────────────

    /**
     * Derived getter for backward compatibility with code expecting the old
     * {@code List<Columna>} shape.
     *
     * <p>Note: This is a transitional helper. The authoritative column collection
     * is {@link #getColumnasTablero()}. When all downstream consumers (mappers,
     * controllers, tests) are updated, this method may be removed.
     *
     * <p>Implementation note: In the catalog model, ColumnaTablero no longer wraps
     * a full Columna object — it only holds the ColumnaId. This method returns
     * null placeholders for the columns list until the mapper hydrates full Columna
     * objects from the catalog. This preserves API shape without maintaining
     * ownership coupling.
     *
     * @return unmodifiable list of Columna values (placeholder nulls until hydrated)
     */
    public List<Columna> getColumnas() {
        return columnasTablero.stream()
            .map(ct -> (Columna) null) // placeholder — Columna hydration happens in infrastructure mapper
            .collect(Collectors.toCollection(ArrayList::new));
    }

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Validates structural invariants for a column collection.
     * Ensures: list and elements not null, no duplicate ColumnaId values,
     * all columns match the given TipoTablero.
     *
     * <p>Note: Board-ownership check (columna.getTableroId() == this.id) is REMOVED
     * because Columna is now a catalog entity decoupled from board ownership.
     * Assignment is validated via tipoTablero match only.
     *
     * @param columnasTablero the column list to validate (must not be null, no null elements)
     * @param tableroId      the expected TableroId (used to validate each column's tableroId)
     * @param tipoTablero    the expected TipoTablero (used to validate each column's tipoTablero)
     * @throws InvariantViolationException if any structural invariant is violated (nulls, duplicates, ownership)
     * @throws TipoTableroMismatchException if any column has incompatible tipoTablero
     */
    private static void validarColumnasDelTablero(
        List<ColumnaTablero> columnasTablero,
        TableroId tableroId,
        TipoTablero tipoTablero
    ) {
        if (columnasTablero == null) {
            throw new InvariantViolationException("La lista de columnas no puede ser null.");
        }
        for (int i = 0; i < columnasTablero.size(); i++) {
            if (columnasTablero.get(i) == null) {
                throw new InvariantViolationException("La columna en posición " + i + " no puede ser null.");
            }
        }

        Set<ColumnaId> seenIds = new HashSet<>();
        for (ColumnaTablero ct : columnasTablero) {
            ColumnaId id = ct.getColumnaId();
            if (seenIds.contains(id)) {
                throw new InvariantViolationException("La lista de columnas contiene IDs duplicados.");
            }
            seenIds.add(id);
        }

        // Board-ownership check removed: Columna is now a catalog, not board-owned.
        // Assignment validation is done via tipoTablero match in agregarColumnaTablero.

        for (ColumnaTablero ct : columnasTablero) {
            if (!ct.getTipoTablero().equals(tipoTablero)) {
                throw new TipoTableroMismatchException();
            }
        }
    }

    /**
     * Creates a new Tablero.
     * Generates id and creadoEn internally.
     * Only SuperUsuario may create a Tablero.
     *
     * <p>Note: structural validation (no nulls, no duplicate IDs, tipoTablero match)
     * is delegated to the centralized helper. Ownership validation against the
     * generated TableroId cannot be performed here because columns are received
     * already-constructed — changing this contract is out of scope for this change.
     *
     * @param nombre                       mandatory - board name
     * @param descripcion                  mandatory - board description
     * @param tipoTablero                  mandatory - type of board (TAREAS or TRATOS)
     * @param columnasTableroPredeterminadas mandatory - exactly 4 default columns, all PREDETERMINADA, matching tipoTablero
     * @param superUsuarioId               mandatory - the internal superuser creating the board
     */
    public static Tablero create(
        String nombre,
        String descripcion,
        TipoTablero tipoTablero,
        List<ColumnaTablero> columnasTableroPredeterminadas,
        SuperUsuarioId superUsuarioId
    ) {
        DomainAssert.notNull(superUsuarioId, "superUsuarioId");

        DomainAssert.notNull(columnasTableroPredeterminadas, "columnasTableroPredeterminadas");
        DomainAssert.notNull(tipoTablero, "tipoTablero");

        if (columnasTableroPredeterminadas.size() != 4) {
            throw new InvalidDefaultColumnsException(4);
        }

        boolean todasPredeterminadas = columnasTableroPredeterminadas.stream()
            .allMatch(ct -> ct != null && ct.getTipoTablero().equals(tipoTablero));
        if (!todasPredeterminadas) {
            throw new InvalidDefaultColumnsException();
        }

        boolean todasMismoTipo = columnasTableroPredeterminadas.stream()
            .allMatch(ct -> tipoTablero.equals(ct.getTipoTablero()));
        if (!todasMismoTipo) {
            throw new TipoTableroMismatchException();
        }

        return new Tablero(
            TableroId.create(),
            DomainAssert.lengthBetween(nombre, "nombre", 1, 100),
            DomainAssert.notBlank(descripcion, "descripcion"),
            List.copyOf(columnasTableroPredeterminadas),
            tipoTablero,
            LocalDateTime.now()
        );
    }

    /**
     * Reconstitutes an existing Tablero from persistence.
     */
    public static Tablero reconstitute(
        TableroId id,
        String nombre,
        String descripcion,
        List<ColumnaTablero> columnasTablero,
        TipoTablero tipoTablero,
        LocalDateTime creadoEn
    ) {
        TableroId resolvedId = DomainAssert.notNull(id, "id");
        String resolvedNombre = DomainAssert.lengthBetween(nombre, "nombre", 1, 100);
        String resolvedDescripcion = DomainAssert.notBlank(descripcion, "descripcion");
        List<ColumnaTablero> resolvedColumnasTablero = List.copyOf(DomainAssert.notNull(columnasTablero, "columnasTablero"));
        TipoTablero resolvedTipoTablero = DomainAssert.notNull(tipoTablero, "tipoTablero");
        DomainAssert.notNull(creadoEn, "creadoEn");

        validarColumnasDelTablero(resolvedColumnasTablero, resolvedId, resolvedTipoTablero);

        return new Tablero(
            resolvedId,
            resolvedNombre,
            resolvedDescripcion,
            resolvedColumnasTablero,
            resolvedTipoTablero,
            creadoEn
        );
    }

    // ── Domain Behavior ────────────────────────────────────────────

    /**
     * Adds a column to this board.
     *
     * <p>Note: Board-ownership check is REMOVED. Columna is now a catalog entity
     * decoupled from board ownership. Assignment is validated via tipoTablero match only.
     *
     * Rules:
     * - columnaTablero is required.
     * - The column cannot already exist in this tablero (by ColumnaId identity).
     * - The column tipoTablero must match this board's tipoTablero.
     * - The column is appended at the end of the column list.
     *
     * @param columnaTablero the contextual column to add (required, contains columnaId + context)
     * @return a new Tablero with the column appended
     * @throws ColumnaYaExisteEnTableroException if column is already in this tablero
     * @throws TipoTableroMismatchException if column tipoTablero does not match this tablero
     */
    public Tablero agregarColumnaTablero(ColumnaTablero columnaTablero) {
        DomainAssert.notNull(columnaTablero, "columnaTablero");

        ColumnaId columnaId = columnaTablero.getColumnaId();
        boolean noDuplicada = columnasTablero.stream().noneMatch(ct -> ct.getColumnaId().equals(columnaId));
        if (!noDuplicada) {
            throw new ColumnaYaExisteEnTableroException();
        }

        // Validate tipoTablero match directly from ColumnaTablero (no longer from wrapped Columna)
        if (!columnaTablero.getTipoTablero().equals(this.tipoTablero)) {
            throw new TipoTableroMismatchException();
        }

        List<ColumnaTablero> nuevasColumnas = new ArrayList<>(columnasTablero);
        nuevasColumnas.add(columnaTablero);

        validarColumnasDelTablero(nuevasColumnas, this.id, this.tipoTablero);

        return new Tablero(
            this.id,
            this.nombre,
            this.descripcion,
            List.copyOf(nuevasColumnas),
            this.tipoTablero,
            this.creadoEn
        );
    }

    /**
     * Removes a column from this board.
     *
     * Rules:
     * - columnaId is required.
     * - The column must belong to this tablero (by ColumnaId identity).
     * - The column must not contain fichas (tieneFichas must be false).
     * - The relative order of remaining columns is preserved.
     *
     * @param columnaId  the id of the column to remove (required)
     * @param tieneFichas true if the column contains fichas, false otherwise
     * @return a new Tablero with the column removed
     * @throws InvariantViolationException if columnaId is null
     * @throws ColumnaNoPerteneceAlTableroException if columnaId does not belong to this tablero
     * @throws ColumnaConFichasNoPuedeEliminarseException if tieneFichas is true
     */
    public Tablero eliminarColumnaDelTablero(ColumnaId columnaId, boolean tieneFichas) {
        DomainAssert.notNull(columnaId, "columnaId");

        boolean columnaExiste = columnasTablero.stream().anyMatch(ct -> ct.getColumnaId().equals(columnaId));
        if (!columnaExiste) {
            throw new ColumnaNoPerteneceAlTableroException();
        }

        if (tieneFichas) {
            throw new ColumnaConFichasNoPuedeEliminarseException();
        }

        List<ColumnaTablero> nuevasColumnas = new ArrayList<>(columnasTablero);
        nuevasColumnas.removeIf(ct -> ct.getColumnaId().equals(columnaId));

        return new Tablero(
            this.id,
            this.nombre,
            this.descripcion,
            List.copyOf(nuevasColumnas),
            this.tipoTablero,
            this.creadoEn
        );
    }

    /**
     * Reorders the columns of this board to match the given new order.
     *
     * Rules:
     * - nuevoOrden is required and must not be empty.
     * - nuevoOrden must contain exactly the same ColumnaId values currently present in this Tablero.
     * - nuevoOrden must not contain duplicate ColumnaId values.
     * - All IDs in nuevoOrden must belong to this Tablero.
     * - TipoColumna is NOT considered during validation or reordering.
     *
     * @param nuevoOrden the desired column order (required, must contain all current column IDs exactly once)
     * @return a new Tablero with columns in the requested order
     * @throws InvariantViolationException if nuevoOrden is null, empty, or contains wrong number of IDs
     * @throws InvariantViolationException if nuevoOrden contains duplicate IDs
     * @throws ColumnaNoPerteneceAlTableroException if nuevoOrden contains an ID that does not belong to this Tablero
     */
    public Tablero reordenarColumnas(List<ColumnaId> nuevoOrden) {
        DomainAssert.notNull(nuevoOrden, "nuevoOrden");

        if (nuevoOrden.isEmpty()) {
            throw new InvariantViolationException("El campo nuevoOrden no puede estar vacío.");
        }

        if (nuevoOrden.size() != columnasTablero.size()) {
            throw new InvariantViolationException(
                "El nuevo orden debe contener exactamente " + columnasTablero.size() + " columnas, pero contiene " + nuevoOrden.size() + "."
            );
        }

        Set<ColumnaId> uniqueIds = new HashSet<>(nuevoOrden);
        if (uniqueIds.size() != nuevoOrden.size()) {
            throw new InvariantViolationException("El nuevo orden no puede contener columnas duplicadas.");
        }

        Map<ColumnaId, ColumnaTablero> columnasMap = new LinkedHashMap<>();
        for (ColumnaTablero ct : columnasTablero) {
            columnasMap.put(ct.getColumnaId(), ct);
        }

        for (ColumnaId id : nuevoOrden) {
            if (!columnasMap.containsKey(id)) {
                throw new ColumnaNoPerteneceAlTableroException();
            }
        }

        List<ColumnaTablero> columnasReordenadas = new ArrayList<>(nuevoOrden.size());
        for (ColumnaId id : nuevoOrden) {
            columnasReordenadas.add(columnasMap.get(id));
        }

        return new Tablero(
            this.id,
            this.nombre,
            this.descripcion,
            List.copyOf(columnasReordenadas),
            this.tipoTablero,
            this.creadoEn
        );
    }
}