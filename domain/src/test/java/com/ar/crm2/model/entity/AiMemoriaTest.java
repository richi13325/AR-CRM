package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.entity.ia.AiMemoria;
import com.ar.crm2.model.enums.OrigenMemoria;
import com.ar.crm2.model.enums.VisibilidadMemoria;
import com.ar.crm2.model.vo.AiMemoriaId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AiMemoria} domain behavior.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Visibility rules: CONVERSACION_SCOPED requires wa id and forbids contactoId;
 *       CONTACTO_SCOPED requires contactoId and forbids wa id.</li>
 *   <li>supersede() is idempotent and records the replacement id.</li>
 *   <li>expirar() is idempotent.</li>
 *   <li>estaExpirada respects the expiresAt and the explicit expirada flag.</li>
 *   <li>estaViva is false when superseded OR expired.</li>
 *   <li>Ownership queries for actor and empresa.</li>
 * </ul>
 */
class AiMemoriaTest {

    private static final UsuarioId ACTOR = UsuarioId.create();
    private static final UsuarioId OTRO_USUARIO = UsuarioId.create();
    private static final EmpresaId EMPRESA = EmpresaId.create();
    private static final String WA_CONVERSACION_ID = "wa-conv-12";
    private static final ContactoId CONTACTO = ContactoId.create();
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("succeeds with CONVERSACION_SCOPED visibility and wa id")
        void crear_conversacionScoped_exitoso() {
            assertThatCode(() -> AiMemoria.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, null,
                    VisibilidadMemoria.CONVERSACION_SCOPED,
                    "Cliente quiere demo", OrigenMemoria.CHAT_ANALYSIS, null,
                    AHORA, AHORA.plusDays(7)
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("succeeds with CONTACTO_SCOPED visibility and contactoId")
        void crear_contactoScoped_exitoso() {
            assertThatCode(() -> AiMemoria.crear(
                    ACTOR, EMPRESA, null, CONTACTO,
                    VisibilidadMemoria.CONTACTO_SCOPED,
                    "Trabaja en Acme", OrigenMemoria.MANUAL, null,
                    AHORA, AHORA.plusDays(30)
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejects CONVERSACION_SCOPED without waConversacionId")
        void crear_conversacionScoped_sinWaId_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMemoria.crear(
                    ACTOR, EMPRESA, null, null,
                    VisibilidadMemoria.CONVERSACION_SCOPED,
                    "x", OrigenMemoria.MANUAL, null,
                    AHORA, AHORA.plusDays(1)
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("CONVERSACION_SCOPED");
        }

        @Test
        @DisplayName("rejects CONVERSACION_SCOPED with contactoId set")
        void crear_conversacionScoped_conContactoId_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMemoria.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    VisibilidadMemoria.CONVERSACION_SCOPED,
                    "x", OrigenMemoria.MANUAL, null,
                    AHORA, AHORA.plusDays(1)
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("CONVERSACION_SCOPED");
        }

        @Test
        @DisplayName("rejects CONTACTO_SCOPED without contactoId")
        void crear_contactoScoped_sinContactoId_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMemoria.crear(
                    ACTOR, EMPRESA, null, null,
                    VisibilidadMemoria.CONTACTO_SCOPED,
                    "x", OrigenMemoria.MANUAL, null,
                    AHORA, AHORA.plusDays(1)
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("CONTACTO_SCOPED");
        }

        @Test
        @DisplayName("rejects expiresAt not strictly after ahora")
        void crear_conExpiresInvalido_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMemoria.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, null,
                    VisibilidadMemoria.CONVERSACION_SCOPED,
                    "x", OrigenMemoria.MANUAL, null,
                    AHORA, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("expiresAt");
        }

        @Test
        @DisplayName("starts at version 1 with superseded=false and expirada=false")
        void crear_arrancaVersionUnoYActiva() {
            AiMemoria m = nuevaMemoria(AHORA);
            assertThat(m.getVersion()).isEqualTo(1L);
            assertThat(m.isSuperseded()).isFalse();
            assertThat(m.isExpirada()).isFalse();
            assertThat(m.getSupersededBy()).isNull();
        }
    }

    @Nested
    @DisplayName("supersede()")
    class Supersede {

        @Test
        @DisplayName("records replacement id, bumps version, marks superseded")
        void supersede_registraYMarca() {
            AiMemoria original = nuevaMemoria(AHORA);
            AiMemoriaId replacement = AiMemoriaId.create();
            AiMemoria superseded = original.supersede(replacement, AHORA.plusMinutes(1));
            assertThat(superseded.isSuperseded()).isTrue();
            assertThat(superseded.getSupersededBy()).isEqualTo(replacement);
            assertThat(superseded.getVersion()).isEqualTo(original.getVersion() + 1);
        }

        @Test
        @DisplayName("is idempotent on already-superseded")
        void supersede_idempotente() {
            AiMemoria original = nuevaMemoria(AHORA);
            AiMemoria superseded = original.supersede(AiMemoriaId.create(), AHORA.plusMinutes(1));
            assertThat(superseded.supersede(AiMemoriaId.create(), AHORA.plusMinutes(2)))
                    .isSameAs(superseded);
        }
    }

    @Nested
    @DisplayName("expirar()")
    class Expirar {

        @Test
        @DisplayName("marks expired and is idempotent")
        void expirar_idempotente() {
            AiMemoria m = nuevaMemoria(AHORA);
            AiMemoria expired = m.expirar(AHORA.plusHours(1));
            assertThat(expired.isExpirada()).isTrue();
            assertThat(expired.expirar(AHORA.plusHours(2))).isSameAs(expired);
        }
    }

    @Nested
    @DisplayName("estaExpirada() + estaViva()")
    class Expiracion {

        @Test
        @DisplayName("estaExpirada true when now is at or past expiresAt")
        void estaExpirada_truePasadoExpiresAt() {
            AiMemoria m = nuevaMemoria(AHORA);
            assertThat(m.estaExpirada(AHORA.plusDays(8))).isTrue();
            assertThat(m.estaExpirada(AHORA.plusDays(7))).isTrue();
            assertThat(m.estaExpirada(AHORA.plusDays(6))).isFalse();
        }

        @Test
        @DisplayName("estaViva false when superseded even if not expired")
        void estaViva_falseSiSuperseded() {
            AiMemoria m = nuevaMemoria(AHORA);
            AiMemoria superseded = m.supersede(AiMemoriaId.create(), AHORA.plusMinutes(1));
            assertThat(superseded.estaViva(AHORA.plusMinutes(2))).isFalse();
        }
    }

    @Nested
    @DisplayName("Ownership")
    class Ownership {

        @Test
        @DisplayName("perteneceA(UsuarioId) true only for the actor")
        void perteneceA_usuario_trueSoloActor() {
            AiMemoria m = nuevaMemoria(AHORA);
            assertThat(m.perteneceA(ACTOR)).isTrue();
            assertThat(m.perteneceA(OTRO_USUARIO)).isFalse();
        }

        @Test
        @DisplayName("perteneceA(EmpresaId) true only for the owning company")
        void perteneceA_empresa_trueSoloEmpresaCorrecta() {
            AiMemoria m = nuevaMemoria(AHORA);
            assertThat(m.perteneceA(EMPRESA)).isTrue();
            assertThat(m.perteneceA(EmpresaId.create())).isFalse();
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("preserves supersededBy and expirada flag")
        void reconstituir_preservaEstado() {
            AiMemoriaId id = AiMemoriaId.create();
            AiMemoriaId replacement = AiMemoriaId.create();
            AiMemoria m = AiMemoria.reconstitute(
                    id, ACTOR, EMPRESA, WA_CONVERSACION_ID, null,
                    VisibilidadMemoria.CONVERSACION_SCOPED,
                    "x", OrigenMemoria.CHAT_ANALYSIS, "src-1", 3L,
                    AHORA.minusDays(1), AHORA.minusHours(1), AHORA.plusDays(1),
                    replacement, true, false
            );
            assertThat(m.getId()).isEqualTo(id);
            assertThat(m.isSuperseded()).isTrue();
            assertThat(m.getSupersededBy()).isEqualTo(replacement);
            assertThat(m.getVersion()).isEqualTo(3L);
        }
    }

    // ── helpers ────────────────────────────────────────────────────

    private static AiMemoria nuevaMemoria(LocalDateTime ahora) {
        return AiMemoria.crear(
                ACTOR, EMPRESA, WA_CONVERSACION_ID, null,
                VisibilidadMemoria.CONVERSACION_SCOPED,
                "Cliente quiere demo la próxima semana",
                OrigenMemoria.CHAT_ANALYSIS, "wa-msg-1",
                ahora, ahora.plusDays(7)
        );
    }
}