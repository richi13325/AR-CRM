package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Ficha} domain behavior.
 * Spring-free — pure domain unit tests.
 */
class FichaTest {

    private static final ColumnaId COLUMNA_ID = ColumnaId.create();
    private static final UsuarioId RESPONSABLE_ID = UsuarioId.create();
    private static final UsuarioId CREADO_POR_ID = UsuarioId.create();
    private static final Instant AHORA = Instant.now();

    // ── create() — TAREA ─────────────────────────────────────────────

    @Nested
    @DisplayName("create() with TAREA")
    class CrearTarea {

        @Test
        @DisplayName("succeeds when tareaId is present and tratoId is null")
        void crearTarea_conTareaIdYsinTratoId_exitoso() {
            TareaId tareaId = TareaId.create();

            assertThatCode(() -> Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,   // tratoId
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when tratoId is present")
        void crearTarea_conTratoId_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            TratoId tratoId = TratoId.create();

            assertThatThrownBy(() -> Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    tratoId,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("no puede tener tratoId");
        }

        @Test
        @DisplayName("throws when tareaId is null")
        void crearTarea_sinTareaId_lanzaExcepcion() {
            assertThatThrownBy(() -> Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    // ── create() — TRATO ─────────────────────────────────────────────

    @Nested
    @DisplayName("create() with TRATO")
    class CrearTrato {

        @Test
        @DisplayName("succeeds when tratoId is present and tareaId is null")
        void crearTrato_conTratoIdYsinTareaId_exitoso() {
            TratoId tratoId = TratoId.create();

            assertThatCode(() -> Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    null,   // tareaId
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when tareaId is present")
        void crearTrato_conTareaId_lanzaExcepcion() {
            TratoId tratoId = TratoId.create();
            TareaId tareaId = TareaId.create();

            assertThatThrownBy(() -> Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("no puede tener tareaId");
        }

        @Test
        @DisplayName("throws when tratoId is null")
        void crearTrato_sinTratoId_lanzaExcepcion() {
            assertThatThrownBy(() -> Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    null,
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    // ── create() — posicion removed ─────────────────────────────────

    @Nested
    @DisplayName("create() does not accept posicion")
    class CrearSinPosicion {

        @Test
        @DisplayName("signature has no posicion parameter")
        void crear_noAdmitePosicion() {
            // Signature verification: compile will fail if posicion reappears
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID
            );
            assertThatCode(() -> { ficha.toString(); }).doesNotThrowAnyException();
        }
    }

    // ── reconstitute() — TAREA ────────────────────────────────────────

    @Nested
    @DisplayName("reconstitute() with TAREA")
    class ReconstituirTarea {

        @Test
        @DisplayName("succeeds when tareaId is present and tratoId is null")
        void reconstituirTarea_conTareaIdYsinTratoId_exitoso() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();

            assertThatCode(() -> Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when tratoId is present")
        void reconstituirTarea_conTratoId_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            TratoId tratoId = TratoId.create();
            FichaId id = FichaId.create();

            assertThatThrownBy(() -> Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    tratoId,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("no puede tener tratoId");
        }

        @Test
        @DisplayName("throws when tareaId is null")
        void reconstituirTarea_sinTareaId_lanzaExcepcion() {
            FichaId id = FichaId.create();

            assertThatThrownBy(() -> Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    // ── reconstitute() — TRATO ────────────────────────────────────────

    @Nested
    @DisplayName("reconstitute() with TRATO")
    class ReconstituirTrato {

        @Test
        @DisplayName("succeeds when tratoId is present and tareaId is null")
        void reconstituirTrato_conTratoIdYsinTareaId_exitoso() {
            TratoId tratoId = TratoId.create();
            FichaId id = FichaId.create();

            assertThatCode(() -> Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when tareaId is present")
        void reconstituirTrato_conTareaId_lanzaExcepcion() {
            TratoId tratoId = TratoId.create();
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();

            assertThatThrownBy(() -> Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            ))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("no puede tener tareaId");
        }

        @Test
        @DisplayName("throws when tratoId is null")
        void reconstituirTrato_sinTratoId_lanzaExcepcion() {
            FichaId id = FichaId.create();

            assertThatThrownBy(() -> Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    null,
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    // ── reconstitute() — posicion removed ───────────────────────────

    @Nested
    @DisplayName("reconstitute() does not accept posicion")
    class ReconstituirSinPosicion {

        @Test
        @DisplayName("signature has no posicion parameter")
        void reconstituir_noAdmitePosicion() {
            // Signature verification: compile will fail if posicion reappears
            TratoId tratoId = TratoId.create();
            FichaId id = FichaId.create();
            Ficha ficha = Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    null,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    AHORA,
                    AHORA
            );
            assertThatCode(() -> { ficha.toString(); }).doesNotThrowAnyException();
        }
    }

    // ── moverAColumna() ─────────────────────────────────────────────

    @Nested
    @DisplayName("moverAColumna()")
    class MoverAColumna {

        @Test
        @DisplayName("throws when nuevaColumnaId is null")
        void moverAColumna_conNull_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();
            Instant haceUnMes = AHORA.minusSeconds(30 * 24 * 60 * 60);
            Ficha ficha = Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    haceUnMes,
                    haceUnMes
            );

            assertThatThrownBy(() -> ficha.moverAColumna(null))
                    .isInstanceOf(InvariantViolationException.class)
                    .hasMessageContaining("nuevaColumnaId");
        }

        @Test
        @DisplayName("returns same instance when target is the same column")
        void moverAColumna_mismaColumna_retornaMismaInstancia() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();
            Instant haceUnMes = AHORA.minusSeconds(30 * 24 * 60 * 60);
            Ficha ficha = Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    tareaId,
                    RESPONSABLE_ID,
                    CREADO_POR_ID,
                    haceUnMes,
                    haceUnMes
            );

            Ficha resultado = ficha.moverAColumna(COLUMNA_ID);

            assertThat(resultado).isSameAs(ficha);
        }

        @Test
        @DisplayName("returns new instance with new columnaId and all other fields preserved")
        void moverAColumna_diferenteColumna_retornaNuevaInstancia() {
            TareaId tareaId = TareaId.create();
            TratoId tratoId = TratoId.create();
            UsuarioId otroResponsable = UsuarioId.create();
            UsuarioId otroCreador = UsuarioId.create();
            ColumnaId otraColumnaId = ColumnaId.create();
            Instant creadoEn = AHORA.minusSeconds(60 * 60);
            Instant actualizadoEn = AHORA.minusSeconds(30 * 60);
            FichaId id = FichaId.create();

            Ficha original = Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    null,
                    otroResponsable,
                    otroCreador,
                    creadoEn,
                    actualizadoEn
            );

            Ficha resultado = original.moverAColumna(otraColumnaId);

            assertThat(resultado).isNotSameAs(original);
            assertThat(resultado.getColumnaId()).isEqualTo(otraColumnaId);
            assertThat(resultado.getId()).isEqualTo(id);
            assertThat(resultado.getTipoFicha()).isEqualTo(TipoFicha.TRATO);
            assertThat(resultado.getTratoId()).isEqualTo(tratoId);
            assertThat(resultado.getTareaId()).isNull();
            assertThat(resultado.getResponsableId()).isEqualTo(otroResponsable);
            assertThat(resultado.getCreadoPor()).isEqualTo(otroCreador);
            assertThat(resultado.getCreadoEn()).isEqualTo(creadoEn);
            assertThat(resultado.getActualizadoEn()).isEqualTo(actualizadoEn);
        }
    }
}