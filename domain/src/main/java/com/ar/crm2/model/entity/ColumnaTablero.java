package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contextual entity representing a Columna's usage within a specific Tablero.
 *
 * <p>Owned by {@link Tablero}. Holds the board-specific WIP limit that cannot exist
 * in the column catalog because it is a property of how the column is used on a board,
 * not a property of the column itself. Also carries an optional note and the
 * persisted total estimated value.
 *
 * <p>Identity: ColumnaId (the assigned catalog column's id).
 * Equality: by all fields (columnaId + tipoTablero + limiteWip + nota + totalValorEstimado).
 *
 * <p>This is NOT a separate entity with its own ID — it is a value-object-like
 * contextual wrapper whose lifecycle is bound to the owning Tablero aggregate.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnaTablero {

    @EqualsAndHashCode.Include
    private final ColumnaId columnaId;

    private final TipoTablero tipoTablero; // stored for validation, not delegated from columna
    private final Integer limiteWip;
    private final String nota;
    private final BigDecimal totalValorEstimado;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new ColumnaTablero for assigning a catalog column to a board.
     *
     * @param columnaId            the catalog ColumnaId to assign (mandatory)
     * @param tipoTablero          the type of board (TAREAS or TRATOS); used for validation
     * @param limiteWip            mandatory WIP limit for this column in this board context; must be > 0
     * @param nota                 optional contextual note; null if blank; if present, max 500 chars
     * @param totalValorEstimado   the persisted total estimated value for this column; non-null, non-negative,
     *                             ZERO for TAREAS columns, any non-negative value for TRATOS columns
     * @throws InvariantViolationException if columnaId or tipoTablero is null,
     *         if limiteWip <= 0,
     *         if totalValorEstimado is null, negative, or non-ZERO for TAREAS columns
     */
    public static ColumnaTablero create(
        ColumnaId columnaId,
        TipoTablero tipoTablero,
        Integer limiteWip,
        String nota,
        BigDecimal totalValorEstimado
    ) {
        DomainAssert.notNull(columnaId, "columnaId");
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        validarLimiteWip(limiteWip);
        validarTotalValorEstimado(totalValorEstimado, tipoTablero);
        String resolvedNota = normalizeOptionalNota(nota);

        return new ColumnaTablero(
            columnaId,
            tipoTablero,
            limiteWip,
            resolvedNota,
            totalValorEstimado
        );
    }

    /**
     * Reconstitutes an existing ColumnaTablero from persistence.
     *
     * @param columnaId           the persisted catalog ColumnaId
     * @param tipoTablero          the board type (TAREAS or TRATOS); used for validation
     * @param limiteWip            the persisted WIP limit; must be > 0
     * @param nota                 the persisted contextual note (may be null)
     * @param totalValorEstimado  the persisted total estimated value; non-null, non-negative,
     *                            ZERO for TAREAS columns, any non-negative value for TRATOS columns
     * @throws InvariantViolationException if columnaId or tipoTablero is null,
     *         if limiteWip <= 0,
     *         if totalValorEstimado is null, negative, or non-ZERO for TAREAS columns
     */
    public static ColumnaTablero reconstitute(
        ColumnaId columnaId,
        TipoTablero tipoTablero,
        Integer limiteWip,
        String nota,
        BigDecimal totalValorEstimado
    ) {
        DomainAssert.notNull(columnaId, "columnaId");
        DomainAssert.notNull(tipoTablero, "tipoTablero");
        validarLimiteWip(limiteWip);
        validarTotalValorEstimado(totalValorEstimado, tipoTablero);
        String resolvedNota = normalizeOptionalNota(nota);

        return new ColumnaTablero(
            columnaId,
            tipoTablero,
            limiteWip,
            resolvedNota,
            totalValorEstimado
        );
    }

    // ── Semantic validation ──────────────────────────────────────

    private static void validarLimiteWip(Integer limiteWip) {
        DomainAssert.positive(limiteWip, "limiteWip", "limiteWip debe ser mayor que cero.");
    }

    private static void validarTotalValorEstimado(BigDecimal total, TipoTablero tipo) {
        DomainAssert.nonNegative(
            total,
            "totalValorEstimado",
            "totalValorEstimado no puede ser negativo."
        );
        if (tipo == TipoTablero.TAREAS && total.compareTo(BigDecimal.ZERO) != 0) {
            DomainAssert.zero(
                total,
                "totalValorEstimado",
                "Las columnas TAREAS deben tener totalValorEstimado igual a cero."
            );
        }
    }

    private static void validarValorEstimado(BigDecimal valor) {
        DomainAssert.notNull(valor, "El valor estimado no puede ser null.", true);
        DomainAssert.nonNegative(valor, "valor", "El valor estimado no puede ser negativo.");
    }

    private static void validarCantidadActualFichas(int cantidad) {
        DomainAssert.nonNegative(cantidad, "La cantidad actual de fichas no puede ser negativa.");
    }

    /**
     * Normalizes the optional contextual note: null/blank becomes null,
     * otherwise validates the length (max 500) and returns the trimmed value.
     */
    private static String normalizeOptionalNota(String nota) {
        if (nota == null || nota.isBlank()) {
            return null;
        }
        DomainAssert.optionalLength(nota, 500, "nota");
        return nota.trim();
    }

    // ── Domain helpers ────────────────────────────────────────────

    /**
     * Returns the catalog column's identity.
     *
     * @return the ColumnaId of the assigned column
     */
    public ColumnaId getColumnaId() {
        return columnaId;
    }

    /**
     * Returns a new ColumnaTablero with the given value added to the total.
     *
     * @param valor the value to add; must be non-null and non-negative
     * @return a new ColumnaTablero instance with updated total
     * @throws InvariantViolationException if valor is null or negative
     */
    public ColumnaTablero agregarValorEstimado(BigDecimal valor) {
        validarValorEstimado(valor);
        BigDecimal nuevoTotal = totalValorEstimado.add(valor);
        return new ColumnaTablero(
            columnaId, tipoTablero, limiteWip, nota, nuevoTotal
        );
    }

    /**
     * Returns a new ColumnaTablero with the given value subtracted from the total.
     *
     * @param valor the value to subtract; must be non-null and non-negative;
     *              must not cause the total to go negative
     * @return a new ColumnaTablero instance with updated total
     * @throws InvariantViolationException if valor is null, negative, or would leave total negative
     */
    public ColumnaTablero quitarValorEstimado(BigDecimal valor) {
        validarValorEstimado(valor);
        BigDecimal subtracted = totalValorEstimado.subtract(valor);
        DomainAssert.nonNegative(
            subtracted,
            "totalValorEstimado",
            "El valor no puede dejar el total negativo."
        );
        return new ColumnaTablero(
            columnaId, tipoTablero, limiteWip, nota, subtracted
        );
    }

    /**
     * Returns a new ColumnaTablero with the total recalculated from the given list of values.
     *
     * <p>Behavior mirrors {@link #calcularTotal(List)} — null/empty list yields ZERO total.
     *
     * @param valoresEstimados list of estimated deal amounts (may contain nulls)
     * @return a new ColumnaTablero instance with recalculated total
     * @throws InvariantViolationException if any value in the list is negative
     */
    public ColumnaTablero recalcularTotalValorEstimado(List<BigDecimal> valoresEstimados) {
        BigDecimal nuevoTotal = calcularTotal(valoresEstimados);
        return new ColumnaTablero(
            columnaId, tipoTablero, limiteWip, nota, nuevoTotal
        );
    }

    /**
     * Checks whether the column has available capacity for additional cards.
     *
     * @param cantidadActualFichas the current number of cards in the column
     * @return true if cantidadActualFichas is strictly less than limiteWip
     * @throws InvariantViolationException if cantidadActualFichas is negative
     */
    public boolean tieneCapacidadDisponible(int cantidadActualFichas) {
        validarCantidadActualFichas(cantidadActualFichas);
        return cantidadActualFichas < limiteWip;
    }

    /**
     * Validates that the column has capacity for the given card count.
     *
     * @param cantidadActualFichas the current number of cards in the column
     * @throws InvariantViolationException if cantidadActualFichas is negative
     *         or greater than or equal to limiteWip
     */
    public void validarCapacidadWip(int cantidadActualFichas) {
        validarCantidadActualFichas(cantidadActualFichas);
        if (cantidadActualFichas >= limiteWip) {
            throw new com.ar.crm2.exception.InvariantViolationException(
                "Se alcanzó el límite WIP de la columna."
            );
        }
    }

    /**
     * Calculates the total of the given estimated deal values.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Null or empty list → returns {@link BigDecimal#ZERO}</li>
     *   <li>Null elements in the list → treated as zero</li>
     *   <li>Negative amounts → {@link com.ar.crm2.exception.InvariantViolationException}</li>
     * </ul>
     *
     * @param valoresEstimados list of estimated deal amounts (may contain nulls)
     * @return the sum of all non-null, non-negative values
     * @throws com.ar.crm2.exception.InvariantViolationException if any value is negative
     */
    public BigDecimal calcularTotal(List<BigDecimal> valoresEstimados) {
        if (valoresEstimados == null || valoresEstimados.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal valor : valoresEstimados) {
            if (valor != null) {
                validarValorEstimado(valor);
                total = total.add(valor);
            }
        }
        return total;
    }
}
