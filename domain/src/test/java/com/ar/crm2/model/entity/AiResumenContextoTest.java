package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiResumenContextoId;
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
 * Unit tests for {@link AiResumenContexto} domain behavior.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Watermark supersession: replacing with a lower watermark is rejected.</li>
 *   <li>esStale(currentWatermark) is true only when current > recorded.</li>
 *   <li>Ownership queries for actor and empresa.</li>
 *   <li>reconstitute() preserves fields.</li>
 * </ul>
 */
class AiResumenContextoTest {

    private static final UsuarioId ACTOR = UsuarioId.create();
    private static final UsuarioId OTRO_USUARIO = UsuarioId.create();
    private static final EmpresaId EMPRESA = EmpresaId.create();
    private static final String WA_CONVERSACION_ID = "wa-conv-9";
    private static final ContactoId CONTACTO = ContactoId.create();
    private static final AiConversacionId CONV = AiConversacionId.create();
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("succeeds with required scope + facts + inferences")
        void crear_exitoso() {
            assertThatCode(() -> AiResumenContexto.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    "Cliente pidió demo", "Intención: demo", "wa-msg-1", 5L,
                    CONV, AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when watermark is negative")
        void crear_conWatermarkNegativo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiResumenContexto.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    "facts", "inferences", null, -1L,
                    CONV, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("sourceWatermark");
        }
    }

    @Nested
    @DisplayName("reemplazarCon()")
    class ReemplazarCon {

        @Test
        @DisplayName("bumps watermark and updates actualizadoEn")
        void reemplazarCon_watermarkMayor_actualiza() {
            AiResumenContexto original = AiResumenContexto.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    "facts v1", "inferences v1", "wa-msg-1", 5L, CONV, AHORA
            );
            AiResumenContexto reemplazo = original.reemplazarCon(
                    "facts v2", "inferences v2", "wa-msg-7", 10L,
                    AHORA.plusMinutes(3)
            );
            assertThat(reemplazo.getId()).isEqualTo(original.getId());
            assertThat(reemplazo.getSourceWatermark()).isEqualTo(10L);
            assertThat(reemplazo.getFacts()).isEqualTo("facts v2");
            assertThat(reemplazo.getActualizadoEn()).isEqualTo(AHORA.plusMinutes(3));
        }

        @Test
        @DisplayName("rejects replacement with a strictly lower watermark")
        void reemplazarCon_watermarkMenor_rechaza() {
            AiResumenContexto original = AiResumenContexto.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    "facts", "inferences", null, 10L, CONV, AHORA
            );
            assertThatThrownBy(() -> original.reemplazarCon(
                    "facts v2", "inferences v2", null, 5L, AHORA.plusMinutes(3)
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("sourceWatermark");
        }
    }

    @Nested
    @DisplayName("Ownership + freshness queries")
    class OwnershipYFreshness {

        @Test
        @DisplayName("perteneceA(UsuarioId) true only for the actor")
        void perteneceA_usuario_trueSoloActor() {
            AiResumenContexto r = nuevoResumen(AHORA);
            assertThat(r.perteneceA(ACTOR)).isTrue();
            assertThat(r.perteneceA(OTRO_USUARIO)).isFalse();
        }

        @Test
        @DisplayName("perteneceA(EmpresaId) true only for the owning company")
        void perteneceA_empresa_trueSoloEmpresaCorrecta() {
            AiResumenContexto r = nuevoResumen(AHORA);
            assertThat(r.perteneceA(EMPRESA)).isTrue();
            assertThat(r.perteneceA(EmpresaId.create())).isFalse();
        }

        @Test
        @DisplayName("esStale is true only when current watermark strictly greater")
        void esStale_trueSoloSiWatermarkActualMayor() {
            AiResumenContexto r = AiResumenContexto.crear(
                    ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    "facts", "inferences", null, 5L, CONV, AHORA
            );
            assertThat(r.esStale(5L)).isFalse();
            assertThat(r.esStale(4L)).isFalse();
            assertThat(r.esStale(6L)).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("preserves id, facts, inferences and watermark")
        void reconstituir_preservaTodo() {
            AiResumenContextoId id = AiResumenContextoId.create();
            AiResumenContexto r = AiResumenContexto.reconstitute(
                    id, ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                    "facts", "inferences", "wa-msg-1", 7L,
                    CONV, AHORA.minusHours(1), AHORA
            );
            assertThat(r.getId()).isEqualTo(id);
            assertThat(r.getFacts()).isEqualTo("facts");
            assertThat(r.getSourceWatermark()).isEqualTo(7L);
        }
    }

    // ── helpers ────────────────────────────────────────────────────

    private static AiResumenContexto nuevoResumen(LocalDateTime ahora) {
        return AiResumenContexto.crear(
                ACTOR, EMPRESA, WA_CONVERSACION_ID, CONTACTO,
                "facts", "inferences", null, 5L, CONV, ahora
        );
    }
}