package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ColumnaTablero} invariants and behavior.
 * Spring-free — pure domain unit tests.
 */
class ColumnaTableroTest {

    private static final TipoTablero TIPO_TAREAS = TipoTablero.TAREAS;
    private static final TipoTablero TIPO_TRATOS = TipoTablero.TRATOS;

    private ColumnaTablero buildColumnaTareas(ColumnaId id, int limiteWip) {
        return ColumnaTablero.create(
            id,
            TIPO_TAREAS,
            limiteWip,
            null,
            BigDecimal.ZERO
        );
    }

    private ColumnaTablero buildColumnaTratos(ColumnaId id, int limiteWip) {
        return ColumnaTablero.create(
            id,
            TIPO_TRATOS,
            limiteWip,
            null,
            BigDecimal.ZERO
        );
    }

    // ── Factory: create() ────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("throw when columnaId is null")
        void nullColumnaId_throws() {
            assertThatThrownBy(() ->
                ColumnaTablero.create(null, TIPO_TAREAS, 5, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throw when tipoTablero is null")
        void nullTipoTablero_throws() {
            ColumnaId id = ColumnaId.create();
            assertThatThrownBy(() ->
                ColumnaTablero.create(id, null, 5, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throw when limiteWip is null")
        void nullLimiteWip_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.create(id, TIPO_TAREAS, null, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throw when limiteWip is zero or negative")
        void nonPositiveLimiteWip_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.create(id, TIPO_TAREAS, 0, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("limiteWip");

            assertThatThrownBy(() ->
                ColumnaTablero.create(id, TIPO_TAREAS, -1, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("limiteWip");
        }

        @Test
        @DisplayName("throw when totalValorEstimado is null")
        void nullTotal_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.create(id, TIPO_TRATOS, 5, null, null)
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("totalValorEstimado");
        }

        @Test
        @DisplayName("throw when totalValorEstimado is negative")
        void negativeTotal_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.create(id, TIPO_TRATOS, 5, null, new BigDecimal("-100.00"))
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("throw when TAREAS column has non-ZERO total")
        void tareasNonZeroTotal_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.create(id, TIPO_TAREAS, 3, null, new BigDecimal("100.00"))
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("TAREAS")
             .hasMessageContaining("cero");
        }

        @Test
        @DisplayName("valid TAREAS column with ZERO total returns ColumnaTablero")
        void validTareas_returnsColumnaTablero() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero result = ColumnaTablero.create(
                id, TIPO_TAREAS, 3, null,
                BigDecimal.ZERO
            );

            assertThat(result.getColumnaId()).isEqualTo(id);
            assertThat(result.getTipoTablero()).isEqualTo(TIPO_TAREAS);
            assertThat(result.getLimiteWip()).isEqualTo(3);
            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("valid TRATOS column returns ColumnaTablero")
        void validTratos_returnsColumnaTablero() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero result = ColumnaTablero.create(
                id, TIPO_TRATOS, 5, null,
                new BigDecimal("1500.00")
            );

            assertThat(result.getColumnaId()).isEqualTo(id);
            assertThat(result.getTipoTablero()).isEqualTo(TIPO_TRATOS);
            assertThat(result.getLimiteWip()).isEqualTo(5);
            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }
    }

    // ── Factory: reconstitute() ─────────────────────────────────

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        @DisplayName("throw when columnaId is null")
        void nullColumnaId_throws() {
            assertThatThrownBy(() ->
                ColumnaTablero.reconstitute(null, TIPO_TAREAS, 5, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throw when tipoTablero is null")
        void nullTipoTablero_throws() {
            ColumnaId id = ColumnaId.create();
            assertThatThrownBy(() ->
                ColumnaTablero.reconstitute(id, null, 5, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throw when limiteWip is null")
        void nullLimiteWip_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.reconstitute(id, TIPO_TAREAS, null, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throw when limiteWip is zero or negative")
        void nonPositiveLimiteWip_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.reconstitute(id, TIPO_TAREAS, 0, null, BigDecimal.ZERO)
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("limiteWip");
        }

        @Test
        @DisplayName("throw when totalValorEstimado is null")
        void nullTotal_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.reconstitute(id, TIPO_TRATOS, 5, null, null)
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("totalValorEstimado");
        }

        @Test
        @DisplayName("throw when TAREAS column has non-ZERO total")
        void tareasNonZeroTotal_throws() {
            ColumnaId id = ColumnaId.create();

            assertThatThrownBy(() ->
                ColumnaTablero.reconstitute(id, TIPO_TAREAS, 10, null, new BigDecimal("100.00"))
            ).isInstanceOf(InvariantViolationException.class)
             .hasMessageContaining("TAREAS");
        }

        @Test
        @DisplayName("valid TAREAS reconstitution returns ColumnaTablero")
        void validTareasReconstitute_returnsColumnaTablero() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero result = ColumnaTablero.reconstitute(
                id, TIPO_TAREAS, 10, null,
                BigDecimal.ZERO
            );

            assertThat(result.getColumnaId()).isEqualTo(id);
            assertThat(result.getLimiteWip()).isEqualTo(10);
            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Nota validation ─────────────────────────────────────────

    @Nested
    @DisplayName("nota validation")
    class NotaValidation {

        @Test
        @DisplayName("null nota stored as null")
        void nullNota_storedAsNull() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero result = ColumnaTablero.create(
                id, TIPO_TAREAS, 3, null,
                BigDecimal.ZERO
            );

            assertThat(result.getNota()).isNull();
        }

        @Test
        @DisplayName("blank nota stored as null")
        void blankNota_storedAsNull() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero result = ColumnaTablero.create(
                id, TIPO_TAREAS, 3, "   ",
                BigDecimal.ZERO
            );

            assertThat(result.getNota()).isNull();
        }

        @Test
        @DisplayName("valid nota trimmed and stored")
        void validNota_trimmedAndStored() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero result = ColumnaTablero.create(
                id, TIPO_TAREAS, 3, "  nota de prueba  ",
                BigDecimal.ZERO
            );

            assertThat(result.getNota()).isEqualTo("nota de prueba");
        }

        @Test
        @DisplayName("nota longer than 500 chars throws")
        void notaTooLong_throws() {
            ColumnaId id = ColumnaId.create();
            String longNota = "a".repeat(501);

            assertThatThrownBy(() -> ColumnaTablero.create(
                    id, TIPO_TAREAS, 3, longNota,
                    BigDecimal.ZERO
                ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("nota");
        }

        @Test
        @DisplayName("nota exactly 500 chars stored")
        void nota500Chars_stored() {
            ColumnaId id = ColumnaId.create();
            String nota500 = "b".repeat(500);

            ColumnaTablero result = ColumnaTablero.create(
                id, TIPO_TAREAS, 3, nota500,
                BigDecimal.ZERO
            );

            assertThat(result.getNota()).isEqualTo(nota500);
        }
    }

    // ── calcularTotal / recalcularTotalValorEstimado ─────────────

    @Nested
    @DisplayName("recalcularTotalValorEstimado(List<BigDecimal>)")
    class RecalcularTotal {

        private ColumnaTablero buildTableroColumnaTratos() {
            ColumnaId id = ColumnaId.create();
            return ColumnaTablero.create(
                id, TIPO_TRATOS, 5, null,
                BigDecimal.ZERO
            );
        }

        @Test
        @DisplayName("null list returns ZERO total")
        void nullList_returnsZero() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            ColumnaTablero result = ct.recalcularTotalValorEstimado(null);
            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("empty list returns ZERO total")
        void emptyList_returnsZero() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            ColumnaTablero result = ct.recalcularTotalValorEstimado(List.of());
            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("list with valid amounts returns correct sum")
        void validAmounts_returnsSum() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            List<BigDecimal> valores = List.of(
                new BigDecimal("100.50"),
                new BigDecimal("200.25")
            );

            ColumnaTablero result = ct.recalcularTotalValorEstimado(valores);

            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("300.75"));
        }

        @Test
        @DisplayName("calcularTotal returns sum without changing persisted total")
        void calcularTotal_returnsSumWithoutChangingPersistedTotal() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            List<BigDecimal> valores = List.of(
                new BigDecimal("100.50"),
                new BigDecimal("200.25")
            );

            BigDecimal total = ct.calcularTotal(valores);

            assertThat(total).isEqualByComparingTo(new BigDecimal("300.75"));
            assertThat(ct.getTotalValorEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("list with null elements treats nulls as zero")
        void nullElements_treatedAsZero() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            List<BigDecimal> valores = Arrays.asList(
                new BigDecimal("100.00"),
                null,
                new BigDecimal("50.00")
            );

            ColumnaTablero result = ct.recalcularTotalValorEstimado(valores);

            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("list containing negative amount throws InvariantViolationException")
        void negativeAmount_throws() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            List<BigDecimal> valores = Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("-50.00")
            );

            assertThatThrownBy(() -> ct.recalcularTotalValorEstimado(valores))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("list with all nulls returns ZERO total")
        void allNulls_returnsZero() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            List<BigDecimal> valores = Arrays.asList(null, null, null);

            ColumnaTablero result = ct.recalcularTotalValorEstimado(valores);

            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("returns new instance, original unchanged")
        void returnsNewInstance() {
            ColumnaTablero ct = buildTableroColumnaTratos();
            BigDecimal originalTotal = ct.getTotalValorEstimado();

            ColumnaTablero result = ct.recalcularTotalValorEstimado(List.of(new BigDecimal("99.00")));

            assertThat(result).isNotSameAs(ct);
            assertThat(ct.getTotalValorEstimado()).isEqualByComparingTo(originalTotal);
            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("99.00"));
        }
    }

    // ── agregarValorEstimado ──────────────────────────────────

    @Nested
    @DisplayName("agregarValorEstimado(BigDecimal)")
    class AgregarValorEstimado {

        private ColumnaTablero buildTratos() {
            ColumnaId id = ColumnaId.create();
            return ColumnaTablero.create(
                id, TIPO_TRATOS, 5, null,
                new BigDecimal("100.00")
            );
        }

        @Test
        @DisplayName("returns new instance with added total")
        void addValue_returnsNewTotal() {
            ColumnaTablero ct = buildTratos();

            ColumnaTablero result = ct.agregarValorEstimado(new BigDecimal("50.00"));

            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(result).isNotSameAs(ct);
            assertThat(ct.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("rejects null value")
        void nullValue_throws() {
            ColumnaTablero ct = buildTratos();

            assertThatThrownBy(() -> ct.agregarValorEstimado(null))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("rejects negative value")
        void negativeValue_throws() {
            ColumnaTablero ct = buildTratos();

            assertThatThrownBy(() -> ct.agregarValorEstimado(new BigDecimal("-10.00")))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("negativo");
        }
    }

    // ── quitarValorEstimado ───────────────────────────────────

    @Nested
    @DisplayName("quitarValorEstimado(BigDecimal)")
    class QuitarValorEstimado {

        private ColumnaTablero buildTratos() {
            ColumnaId id = ColumnaId.create();
            return ColumnaTablero.create(
                id, TIPO_TRATOS, 5, null,
                new BigDecimal("100.00")
            );
        }

        @Test
        @DisplayName("returns new instance with subtracted total")
        void subtractValue_returnsNewTotal() {
            ColumnaTablero ct = buildTratos();

            ColumnaTablero result = ct.quitarValorEstimado(new BigDecimal("30.00"));

            assertThat(result.getTotalValorEstimado()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(result).isNotSameAs(ct);
        }

        @Test
        @DisplayName("rejects null value")
        void nullValue_throws() {
            ColumnaTablero ct = buildTratos();

            assertThatThrownBy(() -> ct.quitarValorEstimado(null))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("rejects value that would leave total negative")
        void valueLeavingNegative_throws() {
            ColumnaTablero ct = buildTratos();

            assertThatThrownBy(() -> ct.quitarValorEstimado(new BigDecimal("200.00")))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("negativo");
        }
    }

    // ── tieneCapacidadDisponible ───────────────────────────────

    @Nested
    @DisplayName("tieneCapacidadDisponible(int)")
    class TieneCapacidadDisponible {

        private ColumnaTablero buildConLimiteWip3() {
            ColumnaId id = ColumnaId.create();
            return ColumnaTablero.create(
                id, TIPO_TAREAS, 3, null,
                BigDecimal.ZERO
            );
        }

        @Test
        @DisplayName("returns true when cantidadActualFichas < limiteWip")
        void belowLimit_returnsTrue() {
            ColumnaTablero ct = buildConLimiteWip3();
            assertThat(ct.tieneCapacidadDisponible(2)).isTrue();
        }

        @Test
        @DisplayName("returns false when cantidadActualFichas == limiteWip")
        void atLimit_returnsFalse() {
            ColumnaTablero ct = buildConLimiteWip3();
            assertThat(ct.tieneCapacidadDisponible(3)).isFalse();
        }

        @Test
        @DisplayName("throws when cantidadActualFichas is negative")
        void negativeCount_throws() {
            ColumnaTablero ct = buildConLimiteWip3();

            assertThatThrownBy(() -> ct.tieneCapacidadDisponible(-1))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("negativa");
        }
    }

    // ── validarCapacidadWip ───────────────────────────────────

    @Nested
    @DisplayName("validarCapacidadWip(int)")
    class ValidarCapacidadWip {

        private ColumnaTablero buildConLimiteWip5() {
            ColumnaId id = ColumnaId.create();
            return ColumnaTablero.create(
                id, TIPO_TAREAS, 5, null,
                BigDecimal.ZERO
            );
        }

        @Test
        @DisplayName("completes successfully when cantidad < limiteWip")
        void belowLimit_ok() {
            ColumnaTablero ct = buildConLimiteWip5();
            ct.validarCapacidadWip(3); // no exception
        }

        @Test
        @DisplayName("throws when cantidad == limiteWip")
        void atLimit_throws() {
            ColumnaTablero ct = buildConLimiteWip5();

            assertThatThrownBy(() -> ct.validarCapacidadWip(5))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("WIP");
        }

        @Test
        @DisplayName("throws when cantidad > limiteWip")
        void aboveLimit_throws() {
            ColumnaTablero ct = buildConLimiteWip5();

            assertThatThrownBy(() -> ct.validarCapacidadWip(6))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("WIP");
        }

        @Test
        @DisplayName("throws when cantidad is negative")
        void negativeCount_throws() {
            ColumnaTablero ct = buildConLimiteWip5();

            assertThatThrownBy(() -> ct.validarCapacidadWip(-1))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("negativa");
        }
    }

    // ── getColumnaId ────────────────────────────────────────────

    @Nested
    @DisplayName("getColumnaId()")
    class GetColumnaId {

        @Test
        @DisplayName("returns the ColumnaId used at construction")
        void returnsColumnaId() {
            ColumnaId id = ColumnaId.create();

            ColumnaTablero ct = ColumnaTablero.create(
                id, TIPO_TAREAS, 5, null,
                BigDecimal.ZERO
            );

            assertThat(ct.getColumnaId()).isEqualTo(id);
        }
    }
}
