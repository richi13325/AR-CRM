package com.ar.crm2.model.entity;

import com.ar.crm2.exception.EtiquetaTypeMismatchException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.vo.EtiquetaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FichaEtiquetaTest {

    @Nested
    @DisplayName("create() valid inputs")
    class CrearValida {

        @Test
        @DisplayName("creates a FichaEtiqueta for TAREA pairing")
        void crear_Tarea_exitoso() {
            EtiquetaId id = EtiquetaId.create();

            FichaEtiqueta fe = FichaEtiqueta.create(id, TipoEtiqueta.TAREA);

            assertThat(fe.getEtiquetaId()).isEqualTo(id);
            assertThat(fe.getTipoEtiqueta()).isEqualTo(TipoEtiqueta.TAREA);
        }

        @Test
        @DisplayName("creates a FichaEtiqueta for TRATO pairing")
        void crear_Trato_exitoso() {
            EtiquetaId id = EtiquetaId.create();

            FichaEtiqueta fe = FichaEtiqueta.create(id, TipoEtiqueta.TRATO);

            assertThat(fe.getTipoEtiqueta()).isEqualTo(TipoEtiqueta.TRATO);
        }
    }

    @Nested
    @DisplayName("create() invalid inputs")
    class CrearInvalida {

        @Test
        @DisplayName("rejects null etiquetaId")
        void crear_etiquetaIdNull_lanzaExcepcion() {
            assertThatThrownBy(() -> FichaEtiqueta.create(null, TipoEtiqueta.TAREA))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("rejects null tipoEtiqueta")
        void crear_tipoNull_lanzaExcepcion() {
            assertThatThrownBy(() -> FichaEtiqueta.create(EtiquetaId.create(), null))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    @Nested
    @DisplayName("matches()")
    class Matches {

        @Test
        @DisplayName("matches the same EtiquetaId as the underlying Etiqueta")
        void matches_mismoId_retornaTrue() {
            EtiquetaId id = EtiquetaId.create();
            FichaEtiqueta fe = FichaEtiqueta.create(id, TipoEtiqueta.TAREA);
            Etiqueta etiqueta = Etiqueta.reconstitute(
                id, "X", TipoEtiqueta.TAREA, "#FFFFFF", java.time.LocalDateTime.now()
            );

            assertThat(fe.matches(etiqueta)).isTrue();
        }

        @Test
        @DisplayName("does not match a different EtiquetaId")
        void matches_distintoId_retornaFalse() {
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TAREA);
            Etiqueta etiqueta = Etiqueta.reconstitute(
                EtiquetaId.create(), "X", TipoEtiqueta.TAREA, "#FFFFFF", java.time.LocalDateTime.now()
            );

            assertThat(fe.matches(etiqueta)).isFalse();
        }
    }

    @Nested
    @DisplayName("assertTypeMatches(Etiqueta)")
    class AssertTypeMatches {

        @Test
        @DisplayName("does not throw when FichaEtiqueta type matches Etiqueta type")
        void assertTypeMatches_mismoTipo_noLanza() {
            EtiquetaId id = EtiquetaId.create();
            FichaEtiqueta fe = FichaEtiqueta.create(id, TipoEtiqueta.TAREA);
            Etiqueta etiqueta = Etiqueta.reconstitute(
                id, "X", TipoEtiqueta.TAREA, "#FFFFFF", java.time.LocalDateTime.now()
            );

            assertThatCode(() -> fe.assertTypeMatches(etiqueta)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws EtiquetaTypeMismatchException when types differ")
        void assertTypeMatches_tipoDistinto_lanzaExcepcion() {
            EtiquetaId id = EtiquetaId.create();
            FichaEtiqueta fe = FichaEtiqueta.create(id, TipoEtiqueta.TAREA);
            Etiqueta etiqueta = Etiqueta.reconstitute(
                id, "X", TipoEtiqueta.TRATO, "#FFFFFF", java.time.LocalDateTime.now()
            );

            assertThatThrownBy(() -> fe.assertTypeMatches(etiqueta))
                .isInstanceOf(EtiquetaTypeMismatchException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("reconstitutes without re-validation beyond nulls")
        void reconstituir_exitoso() {
            EtiquetaId id = EtiquetaId.create();

            FichaEtiqueta fe = FichaEtiqueta.reconstitute(id, TipoEtiqueta.TAREA);

            assertThat(fe.getEtiquetaId()).isEqualTo(id);
            assertThat(fe.getTipoEtiqueta()).isEqualTo(TipoEtiqueta.TAREA);
        }
    }
}
