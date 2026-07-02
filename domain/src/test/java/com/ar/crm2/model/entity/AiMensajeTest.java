package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiMensajeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AiMensaje} domain behavior.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Required-field validation (aiConversacionId, rol, contenido).</li>
 *   <li>Token/latency counters must be non-negative when provided.</li>
 *   <li>Tool-call JSON is stored opaquely (domain does not parse it).</li>
 *   <li>Ownership query belongs to the parent AiConversacion.</li>
 * </ul>
 */
class AiMensajeTest {

    private static final AiConversacionId CONV = AiConversacionId.create();
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("succeeds with minimal required fields")
        void crear_minimo_exitoso() {
            assertThatCode(() -> AiMensaje.crear(
                    CONV, RolMensajeAi.USER, "Hola", null,
                    null, null, null, null, AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("succeeds with assistant metadata (tokens + latency + model)")
        void crear_conMetadatosAsistente_exitoso() {
            AiMensaje m = AiMensaje.crear(
                    CONV, RolMensajeAi.ASSISTANT, "Respuesta del modelo",
                    "gpt-4o-mini", 120, 80, 250L,
                    null, AHORA
            );
            assertThat(m.getModelo()).isEqualTo("gpt-4o-mini");
            assertThat(m.getPromptTokens()).isEqualTo(120);
            assertThat(m.getCompletionTokens()).isEqualTo(80);
            assertThat(m.getLatencyMs()).isEqualTo(250L);
        }

        @Test
        @DisplayName("preserves toolCallJson opaquely without interpreting it")
        void crear_toolCallJsonOpaco() {
            String toolJson = "{\"name\":\"ProponerAccion\",\"args\":{\"tipo\":\"CREATE_TAREA\"}}";
            AiMensaje m = AiMensaje.crear(
                    CONV, RolMensajeAi.TOOL, "tool-output", null,
                    null, null, null, toolJson, AHORA
            );
            assertThat(m.getToolCallJson()).isEqualTo(toolJson);
        }

        @Test
        @DisplayName("throws when contenido is blank")
        void crear_conContenidoBlanco_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMensaje.crear(
                    CONV, RolMensajeAi.USER, "   ", null,
                    null, null, null, null, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("contenido");
        }

        @Test
        @DisplayName("throws when rol is null")
        void crear_conRolNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMensaje.crear(
                    CONV, null, "Hola", null,
                    null, null, null, null, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("rol");
        }

        @Test
        @DisplayName("throws when promptTokens is negative")
        void crear_conPromptTokensNegativo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMensaje.crear(
                    CONV, RolMensajeAi.ASSISTANT, "ok", null,
                    -1, null, null, null, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("promptTokens");
        }

        @Test
        @DisplayName("throws when latencyMs is negative")
        void crear_conLatencyNegativo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiMensaje.crear(
                    CONV, RolMensajeAi.ASSISTANT, "ok", null,
                    null, null, -5L, null, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("latencyMs");
        }
    }

    @Nested
    @DisplayName("perteneceA()")
    class Ownership {

        @Test
        @DisplayName("true only for the owning AI conversation")
        void perteneceA_trueSoloConversacionOrigen() {
            AiMensaje m = AiMensaje.crear(
                    CONV, RolMensajeAi.USER, "Hola", null,
                    null, null, null, null, AHORA
            );
            assertThat(m.perteneceA(CONV)).isTrue();
            assertThat(m.perteneceA(AiConversacionId.create())).isFalse();
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("preserves id and created timestamp")
        void reconstituir_preservaIdentidadYCreadoEn() {
            AiMensajeId id = AiMensajeId.create();
            AiMensaje m = AiMensaje.reconstitute(
                    id, CONV, RolMensajeAi.SYSTEM, "system-prompt", "gpt-4o-mini",
                    50, 0, 100L, null, AHORA.minusMinutes(2)
            );
            assertThat(m.getId()).isEqualTo(id);
            assertThat(m.getRol()).isEqualTo(RolMensajeAi.SYSTEM);
            assertThat(m.getCreadoEn()).isEqualTo(AHORA.minusMinutes(2));
        }
    }
}