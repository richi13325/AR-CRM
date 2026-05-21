package com.ar.crm2.model.entity;

import com.ar.crm2.exception.ColumnaEnUsoNoPuedeEliminarseException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.exception.NombreColumnaYaExisteException;
import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.SuperUsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Columna} domain behavior.
 * Spring-free — pure domain unit tests.
 */
class ColumnaTest {

    private static final TipoTablero TIPO = TipoTablero.TAREAS;

    private Columna buildColumna(String nombre, ColumnaId id) {
        return Columna.reconstitute(
            id,
            nombre,
            "#FFFFFF",
            TIPO,
            TipoColumna.PREDETERMINADA,
            false
        );
    }

    // ── eliminar() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("eliminar(boolean estaEnUsoEnTablero)")
    class Eliminar {

        @Test
        @DisplayName("throws ColumnaEnUsoNoPuedeEliminarseException when column is in use")
        void eliminar_cuandoEstaEnUso_lanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            Columna columna = buildColumna("Por Hacer", id);

            assertThatThrownBy(() -> columna.eliminar(true))
                .isInstanceOf(ColumnaEnUsoNoPuedeEliminarseException.class);
        }

        @Test
        @DisplayName("completes without exception when column is not in use")
        void eliminar_cuandoNoEstaEnUso_noLanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            Columna columna = buildColumna("Por Hacer", id);

            assertThatCode(() -> columna.eliminar(false))
                .doesNotThrowAnyException();
        }
    }

    // ── create() with duplicate flag ─────────────────────────────────

    @Nested
    @DisplayName("create(..., boolean existeOtraColumnaConMismoNombre)")
    class Crear {

        private static final SuperUsuarioId SUPER_USUARIO_ID = SuperUsuarioId.create();

        @Test
        @DisplayName("throws NombreColumnaYaExisteException when duplicate flag is true")
        void crear_conDuplicateFlagTrue_lanzaExcepcion() {
            assertThatThrownBy(() -> Columna.create(
                    SUPER_USUARIO_ID,
                    "Por Hacer",
                    TIPO,
                    TipoColumna.PREDETERMINADA,
                    "#FFFFFF",
                    true
                ))
                .isInstanceOf(NombreColumnaYaExisteException.class);
        }

        @Test
        @DisplayName("completes without exception when duplicate flag is false")
        void crear_conDuplicateFlagFalse_noLanzaExcepcion() {
            assertThatCode(() -> Columna.create(
                    SUPER_USUARIO_ID,
                    "Por Hacer",
                    TIPO,
                    TipoColumna.PREDETERMINADA,
                    "#FFFFFF",
                    false
                ))
                .doesNotThrowAnyException();
        }
    }

    // ── reconstitute() with duplicate flag ───────────────────────────

    @Nested
    @DisplayName("reconstitute(..., boolean existeOtraColumnaConMismoNombre)")
    class Reconstituir {

        @Test
        @DisplayName("throws NombreColumnaYaExisteException when duplicate flag is true")
        void reconstituir_conDuplicateFlagTrue_lanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            assertThatThrownBy(() -> Columna.reconstitute(
                    id,
                    "Por Hacer",
                    "#FFFFFF",
                    TIPO,
                    TipoColumna.PREDETERMINADA,
                    true
                ))
                .isInstanceOf(NombreColumnaYaExisteException.class);
        }

        @Test
        @DisplayName("does not throw when duplicate flag is false")
        void reconstituir_conDuplicateFlagFalse_noLanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            assertThatCode(() -> Columna.reconstitute(
                    id,
                    "Por Hacer",
                    "#FFFFFF",
                    TIPO,
                    TipoColumna.PREDETERMINADA,
                    false
                ))
                .doesNotThrowAnyException();
        }
    }

    // ── renombrar() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("renombrar(String nuevoNombre, boolean existeOtraColumnaConMismoNombre)")
    class Renombrar {

        @Test
        @DisplayName("returns new Columna with new name and all other fields preserved")
        void renombrar_exito_preservaCampos() {
            ColumnaId id = ColumnaId.create();
            Columna original = Columna.reconstitute(
                id,
                "Por Hacer",
                "#FFFFFF",
                TIPO,
                TipoColumna.PREDETERMINADA,
                false
            );

            Columna renamed = original.renombrar("En Progreso", false);

            assertThat(renamed.getId()).isEqualTo(id);
            assertThat(renamed.getColumnanombre()).isEqualTo("En Progreso");
            assertThat(renamed.getColor()).isEqualTo("#FFFFFF");
            assertThat(renamed.getTipoTablero()).isEqualTo(TIPO);
            assertThat(renamed.getTipoColumna()).isEqualTo(TipoColumna.PREDETERMINADA);
        }

        @Test
        @DisplayName("throws NombreColumnaYaExisteException when duplicate flag is true")
        void renombrar_conDuplicateFlagTrue_lanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            Columna original = Columna.reconstitute(
                id,
                "Por Hacer",
                "#FFFFFF",
                TIPO,
                TipoColumna.PREDETERMINADA,
                false
            );

            assertThatThrownBy(() -> original.renombrar("Nuevo Nombre", true))
                .isInstanceOf(NombreColumnaYaExisteException.class);
        }

        @Test
        @DisplayName("throws NombreColumnaYaExisteException when renaming to existing name")
        void renombrar_aNombreExistente_lanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            Columna original = Columna.reconstitute(
                id,
                "Por Hacer",
                "#FFFFFF",
                TIPO,
                TipoColumna.PREDETERMINADA,
                false
            );

            assertThatThrownBy(() -> original.renombrar("Por Hacer", true))
                .isInstanceOf(NombreColumnaYaExisteException.class);
        }

        @Test
        @DisplayName("throws InvariantViolationException when renaming to blank name")
        void renombrar_nombreBlank_lanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            Columna original = Columna.reconstitute(
                id,
                "Por Hacer",
                "#FFFFFF",
                TIPO,
                TipoColumna.PREDETERMINADA,
                false
            );

            assertThatThrownBy(() -> original.renombrar("   ", false))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("throws InvariantViolationException when renaming to name longer than 80 characters")
        void renombrar_nombreMuyLargo_lanzaExcepcion() {
            ColumnaId id = ColumnaId.create();
            Columna original = Columna.reconstitute(
                id,
                "Por Hacer",
                "#FFFFFF",
                TIPO,
                TipoColumna.PREDETERMINADA,
                false
            );
            String nombreLargo = "A".repeat(81);

            assertThatThrownBy(() -> original.renombrar(nombreLargo, false))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("original Columna remains unchanged after rename — immutable")
        void renombrar_inmutable() {
            ColumnaId id = ColumnaId.create();
            Columna original = Columna.reconstitute(
                id,
                "Por Hacer",
                "#FFFFFF",
                TIPO,
                TipoColumna.PREDETERMINADA,
                false
            );

            Columna renamed = original.renombrar("En Progreso", false);

            assertThat(original.getColumnanombre()).isEqualTo("Por Hacer");
            assertThat(original).isNotSameAs(renamed);
        }
    }
}