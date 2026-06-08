package com.ar.crm2.model.entity;

import com.ar.crm2.exception.EtiquetaTypeMismatchException;
import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.TipoEtiqueta;
import com.ar.crm2.model.enums.TipoFicha;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.EtiquetaId;
import com.ar.crm2.model.vo.FichaId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FichaTest {

    private static final ColumnaId COLUMNA_ID = ColumnaId.create();
    private static final Instant AHORA = Instant.now();

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
                    null,
                    tareaId
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
                    tareaId
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
                    null
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

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
                    null
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
                    tareaId
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
                    null
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    @Nested
    @DisplayName("create() does not accept posicion")
    class CrearSinPosicion {

        @Test
        @DisplayName("signature has no posicion parameter")
        void crear_noAdmitePosicion() {
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(
                    COLUMNA_ID,
                    TipoFicha.TAREA,
                    null,
                    tareaId
            );
            assertThatCode(() -> { ficha.toString(); }).doesNotThrowAnyException();
        }
    }

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
                    AHORA
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

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
                    AHORA
            ))
                .isInstanceOf(InvariantViolationException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute() does not accept posicion")
    class ReconstituirSinPosicion {

        @Test
        @DisplayName("signature has no posicion parameter")
        void reconstituir_noAdmitePosicion() {
            TratoId tratoId = TratoId.create();
            FichaId id = FichaId.create();
            Ficha ficha = Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    null,
                    AHORA
            );
            assertThatCode(() -> { ficha.toString(); }).doesNotThrowAnyException();
        }
    }

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
                    haceUnMes
            );

            Ficha resultado = ficha.moverAColumna(COLUMNA_ID);

            assertThat(resultado).isSameAs(ficha);
        }

        @Test
        @DisplayName("returns new instance with new columnaId and updated timestamp")
        void moverAColumna_diferenteColumna_retornaNuevaInstancia() {
            TareaId tareaId = TareaId.create();
            TratoId tratoId = TratoId.create();
            ColumnaId otraColumnaId = ColumnaId.create();
            Instant actualizadoEn = AHORA.minusSeconds(30 * 60);
            FichaId id = FichaId.create();

            Ficha original = Ficha.reconstitute(
                    id,
                    COLUMNA_ID,
                    TipoFicha.TRATO,
                    tratoId,
                    null,
                    actualizadoEn
            );

            Instant antes = Instant.now();
            Ficha resultado = original.moverAColumna(otraColumnaId);
            Instant despues = Instant.now();

            assertThat(resultado).isNotSameAs(original);
            assertThat(resultado.getColumnaId()).isEqualTo(otraColumnaId);
            assertThat(resultado.getId()).isEqualTo(id);
            assertThat(resultado.getTipoFicha()).isEqualTo(TipoFicha.TRATO);
            assertThat(resultado.getTratoId()).isEqualTo(tratoId);
            assertThat(resultado.getTareaId()).isNull();
            assertThat(resultado.getActualizadoEn()).isAfterOrEqualTo(antes);
            assertThat(resultado.getActualizadoEn()).isBeforeOrEqualTo(despues);
        }
    }

    @Nested
    @DisplayName("withEtiquetas() for TAREA")
    class WithEtiquetasTarea {

        @Test
        @DisplayName("succeeds with empty list of etiquetas")
        void withEtiquetas_listaVacia_exitoso() {
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TAREA, null, tareaId);

            Ficha updated = ficha.withEtiquetas(List.of());

            assertThat(updated.getEtiquetas()).isEmpty();
            assertThat(updated.getId()).isEqualTo(ficha.getId());
            assertThat(updated.getTipoFicha()).isEqualTo(TipoFicha.TAREA);
        }

        @Test
        @DisplayName("succeeds with a single TAREA etiqueta")
        void withEtiquetas_unEtiquetaTarea_exitoso() {
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TAREA, null, tareaId);
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TAREA);

            Ficha updated = ficha.withEtiquetas(List.of(fe));

            assertThat(updated.getEtiquetas()).hasSize(1);
            assertThat(updated.getEtiquetas().get(0)).isEqualTo(fe);
        }

        @Test
        @DisplayName("succeeds with multiple TAREA etiquetas")
        void withEtiquetas_multiplesEtiquetasTarea_exitoso() {
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TAREA, null, tareaId);
            FichaEtiqueta fe1 = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TAREA);
            FichaEtiqueta fe2 = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TAREA);

            Ficha updated = ficha.withEtiquetas(List.of(fe1, fe2));

            assertThat(updated.getEtiquetas()).hasSize(2);
        }

        @Test
        @DisplayName("rejects duplicate EtiquetaId in the list")
        void withEtiquetas_etiquetasDuplicadas_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TAREA, null, tareaId);
            EtiquetaId dupId = EtiquetaId.create();
            FichaEtiqueta fe1 = FichaEtiqueta.create(dupId, TipoEtiqueta.TAREA);
            FichaEtiqueta fe2 = FichaEtiqueta.create(dupId, TipoEtiqueta.TAREA);

            assertThatThrownBy(() -> ficha.withEtiquetas(List.of(fe1, fe2)))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("duplicad");
        }

        @Test
        @DisplayName("rejects TRATO etiqueta on a TAREA Ficha")
        void withEtiquetas_tipoIncorrecto_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TAREA, null, tareaId);
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TRATO);

            assertThatThrownBy(() -> ficha.withEtiquetas(List.of(fe)))
                .isInstanceOf(EtiquetaTypeMismatchException.class);
        }
    }

    @Nested
    @DisplayName("withEtiquetas() for TRATO")
    class WithEtiquetasTrato {

        @Test
        @DisplayName("succeeds with a TRATO etiqueta")
        void withEtiquetas_etiquetaTrato_exitoso() {
            TratoId tratoId = TratoId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TRATO, tratoId, null);
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TRATO);

            Ficha updated = ficha.withEtiquetas(List.of(fe));

            assertThat(updated.getEtiquetas()).hasSize(1);
        }

        @Test
        @DisplayName("rejects TAREA etiqueta on a TRATO Ficha")
        void withEtiquetas_tipoIncorrecto_lanzaExcepcion() {
            TratoId tratoId = TratoId.create();
            Ficha ficha = Ficha.create(COLUMNA_ID, TipoFicha.TRATO, tratoId, null);
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TAREA);

            assertThatThrownBy(() -> ficha.withEtiquetas(List.of(fe)))
                .isInstanceOf(EtiquetaTypeMismatchException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute() preserves etiquetas list")
    class ReconstituirPreservaEtiquetas {

        @Test
        @DisplayName("reconstitute preserves an empty etiquetas list")
        void reconstituir_etiquetasVacias_exitoso() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();

            Ficha ficha = Ficha.reconstitute(
                id, COLUMNA_ID, TipoFicha.TAREA, null, tareaId, AHORA, List.of()
            );

            assertThat(ficha.getEtiquetas()).isEmpty();
        }

        @Test
        @DisplayName("reconstitute preserves a non-empty etiquetas list")
        void reconstituir_etiquetasNoVacias_exitoso() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TAREA);

            Ficha ficha = Ficha.reconstitute(
                id, COLUMNA_ID, TipoFicha.TAREA, null, tareaId, AHORA, List.of(fe)
            );

            assertThat(ficha.getEtiquetas()).hasSize(1);
            assertThat(ficha.getEtiquetas().get(0)).isEqualTo(fe);
        }

        @Test
        @DisplayName("reconstitute rejects mismatched type in etiquetas list")
        void reconstituir_tipoIncorrecto_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();
            FichaEtiqueta fe = FichaEtiqueta.create(EtiquetaId.create(), TipoEtiqueta.TRATO);

            assertThatThrownBy(() -> Ficha.reconstitute(
                id, COLUMNA_ID, TipoFicha.TAREA, null, tareaId, AHORA, List.of(fe)))
                .isInstanceOf(EtiquetaTypeMismatchException.class);
        }

        @Test
        @DisplayName("reconstitute rejects duplicate EtiquetaId in etiquetas list")
        void reconstituir_duplicado_lanzaExcepcion() {
            TareaId tareaId = TareaId.create();
            FichaId id = FichaId.create();
            EtiquetaId dupId = EtiquetaId.create();

            assertThatThrownBy(() -> Ficha.reconstitute(
                id, COLUMNA_ID, TipoFicha.TAREA, null, tareaId, AHORA,
                List.of(
                    FichaEtiqueta.create(dupId, TipoEtiqueta.TAREA),
                    FichaEtiqueta.create(dupId, TipoEtiqueta.TAREA)
                )))
                .isInstanceOf(InvariantViolationException.class);
        }
    }
}
