package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvalidColorFormatException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.EtiquetaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtiquetaTest {

    private static final EtiquetaId ID = EtiquetaId.create();

    @Nested
    @DisplayName("create() valid inputs")
    class CrearValida {

        @Test
        @DisplayName("creates an Etiqueta for TAREA with a valid name and color")
        void crear_Tarea_exitoso() {
            Etiqueta etiqueta = Etiqueta.create("Urgent", TipoEtiqueta.TAREA, "#FF0000");

            assertThat(etiqueta.getId()).isNotNull();
            assertThat(etiqueta.getNombre()).isEqualTo("Urgent");
            assertThat(etiqueta.getTipoEtiqueta()).isEqualTo(TipoEtiqueta.TAREA);
            assertThat(etiqueta.getColor()).isEqualTo("#FF0000");
            assertThat(etiqueta.getCreadoEn()).isNotNull();
        }

        @Test
        @DisplayName("creates an Etiqueta for TRATO with a valid name and color")
        void crear_Trato_exitoso() {
            Etiqueta etiqueta = Etiqueta.create("Premium", TipoEtiqueta.TRATO, "#00AAFF");

            assertThat(etiqueta.getId()).isNotNull();
            assertThat(etiqueta.getTipoEtiqueta()).isEqualTo(TipoEtiqueta.TRATO);
            assertThat(etiqueta.getColor()).isEqualTo("#00AAFF");
        }

        @Test
        @DisplayName("trims name whitespace before persisting")
        void crear_normalizaNombre() {
            Etiqueta etiqueta = Etiqueta.create("  Urgent  ", TipoEtiqueta.TAREA, "#FFFFFF");

            assertThat(etiqueta.getNombre()).isEqualTo("Urgent");
        }

        @Test
        @DisplayName("accepts a 7-character hex color")
        void crear_colorHex7Aceptado() {
            assertThatCode(() -> Etiqueta.create("X", TipoEtiqueta.TAREA, "#ABCDEF"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("create() invalid inputs")
    class CrearInvalida {

        @Test
        @DisplayName("rejects null name")
        void crear_nombreNull_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create(null, TipoEtiqueta.TAREA, "#FFFFFF"))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("rejects blank name")
        void crear_nombreBlank_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("   ", TipoEtiqueta.TAREA, "#FFFFFF"))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("rejects null tipoEtiqueta")
        void crear_tipoNull_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("X", null, "#FFFFFF"))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("rejects null color")
        void crear_colorNull_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("X", TipoEtiqueta.TAREA, null))
                .isInstanceOf(InvalidColorFormatException.class);
        }

        @Test
        @DisplayName("rejects blank color")
        void crear_colorBlank_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("X", TipoEtiqueta.TAREA, "   "))
                .isInstanceOf(InvalidColorFormatException.class);
        }

        @Test
        @DisplayName("rejects color without leading hash")
        void crear_colorSinHash_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("X", TipoEtiqueta.TAREA, "FF0000"))
                .isInstanceOf(InvalidColorFormatException.class);
        }

        @Test
        @DisplayName("rejects color with non-hex characters")
        void crear_colorNoHex_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("X", TipoEtiqueta.TAREA, "#ZZZZZZ"))
                .isInstanceOf(InvalidColorFormatException.class);
        }

        @Test
        @DisplayName("rejects color with wrong length")
        void crear_colorLongitudIncorrecta_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.create("X", TipoEtiqueta.TAREA, "#FFF"))
                .isInstanceOf(InvalidColorFormatException.class);
        }
    }

    @Nested
    @DisplayName("rename() and recolor()")
    class RenombrarYRecolorear {

        @Test
        @DisplayName("rename returns a new Etiqueta with updated name preserving identity")
        void renombrar_actualizaNombre() {
            Etiqueta original = Etiqueta.create("Old", TipoEtiqueta.TAREA, "#FFFFFF");

            Etiqueta renamed = original.rename("New");

            assertThat(renamed).isNotSameAs(original);
            assertThat(renamed.getId()).isEqualTo(original.getId());
            assertThat(renamed.getNombre()).isEqualTo("New");
            assertThat(renamed.getColor()).isEqualTo(original.getColor());
            assertThat(renamed.getTipoEtiqueta()).isEqualTo(original.getTipoEtiqueta());
        }

        @Test
        @DisplayName("rename rejects blank name")
        void renombrar_nombreBlank_lanzaExcepcion() {
            Etiqueta original = Etiqueta.create("Old", TipoEtiqueta.TAREA, "#FFFFFF");

            assertThatThrownBy(() -> original.rename("   "))
                .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        @DisplayName("recolor returns a new Etiqueta with updated color preserving identity")
        void recolorear_actualizaColor() {
            Etiqueta original = Etiqueta.create("X", TipoEtiqueta.TAREA, "#FFFFFF");

            Etiqueta recolored = original.recolor("#000000");

            assertThat(recolored).isNotSameAs(original);
            assertThat(recolored.getId()).isEqualTo(original.getId());
            assertThat(recolored.getNombre()).isEqualTo(original.getNombre());
            assertThat(recolored.getColor()).isEqualTo("#000000");
        }

        @Test
        @DisplayName("recolor rejects invalid color")
        void recolorear_colorInvalido_lanzaExcepcion() {
            Etiqueta original = Etiqueta.create("X", TipoEtiqueta.TAREA, "#FFFFFF");

            assertThatThrownBy(() -> original.recolor("not-a-color"))
                .isInstanceOf(InvalidColorFormatException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("reconstitutes an existing Etiqueta without re-validating uniqueness")
        void reconstituir_sinValidarUnicidad() {
            EtiquetaId id = EtiquetaId.from(UUID.randomUUID());
            LocalDateTime creadoEn = LocalDateTime.now().minusDays(1);

            Etiqueta etiqueta = Etiqueta.reconstitute(id, "X", TipoEtiqueta.TAREA, "#FFFFFF", creadoEn);

            assertThat(etiqueta.getId()).isEqualTo(id);
            assertThat(etiqueta.getNombre()).isEqualTo("X");
            assertThat(etiqueta.getCreadoEn()).isEqualTo(creadoEn);
        }

        @Test
        @DisplayName("reconstitute rejects null id")
        void reconstituir_idNull_lanzaExcepcion() {
            assertThatThrownBy(() -> Etiqueta.reconstitute(
                null, "X", TipoEtiqueta.TAREA, "#FFFFFF", LocalDateTime.now()))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    @Nested
    @DisplayName("TipoEtiqueta.fromFicha()")
    class TipoEtiquetaFromFicha {

        @Test
        @DisplayName("maps TipoFicha.TAREA to TipoEtiqueta.TAREA")
        void fromFicha_tarea() {
            assertThat(TipoEtiqueta.fromFicha(TipoFicha.TAREA)).isEqualTo(TipoEtiqueta.TAREA);
        }

        @Test
        @DisplayName("maps TipoFicha.TRATO to TipoEtiqueta.TRATO")
        void fromFicha_trato() {
            assertThat(TipoEtiqueta.fromFicha(TipoFicha.TRATO)).isEqualTo(TipoEtiqueta.TRATO);
        }
    }
}
