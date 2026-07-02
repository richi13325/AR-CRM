package com.ar.crm2.model.entity.ia;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.exception.AccionStateTransitionException;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
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
 * Unit tests for {@link AiAccion} domain behavior.
 * Spring-free — pure domain unit tests.
 *
 * <p>Coverage targets for the PR 1 sample gate:
 * <ul>
 *   <li>Ownership scope: id, empresaId, solicitadaPor are preserved.</li>
 *   <li>Payload opacity: domain stores payloadJson as-is without interpreting it.</li>
 *   <li>Version monotonic: version starts at 1 and increments on every transition.</li>
 *   <li>State machine: only valid PENDING transitions are allowed; terminal states reject further moves.</li>
 *   <li>Expiry: estaExpirada semantics for PENDING only.</li>
 * </ul>
 */
class AiAccionTest {

    private static final UsuarioId SOLICITANTE = UsuarioId.create();
    private static final UsuarioId OTRO_USUARIO = UsuarioId.create();
    private static final EmpresaId EMPRESA = EmpresaId.create();
    private static final AiConversacionId AI_CONVERSACION_ID = AiConversacionId.create();
    private static final String WA_CONVERSACION_ID = "wa-conv-123";
    private static final String TIPO_ACCION = "CREATE_TAREA";
    private static final String PAYLOAD_JSON = "{\"titulo\":\"Llamar cliente\",\"prioridad\":\"ALTA\"}";
    private static final String RATIONALE = "Cliente pidió demo en chat";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    // ── crear() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("succeeds when required fields are provided")
        void crear_conCamposRequeridos_exitoso() {
            assertThatCode(() -> AiAccion.crear(
                    EMPRESA,
                    SOLICITANTE,
                    WA_CONVERSACION_ID,
                    null,
                    AI_CONVERSACION_ID,
                    TIPO_ACCION,
                    PAYLOAD_JSON,
                    RATIONALE,
                    60,
                    AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("preserves payloadJson opaquely without interpreting it")
        void crear_payloadSePreservaOpaco() {
            String payloadArbitrario = "{\"cualquier\":\"cosa\",\"con\":[\"arrays\"]}";
            AiAccion p = AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, payloadArbitrario, RATIONALE, 60, AHORA
            );
            assertThat(p.getPayloadJson()).isEqualTo(payloadArbitrario);
        }

        @Test
        @DisplayName("starts at version 1 with estado PENDING")
        void crear_versionInicialUnoYPending() {
            AiAccion p = AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, AHORA
            );
            assertThat(p.getVersion()).isEqualTo(1);
            assertThat(p.getEstado()).isEqualTo(EstadoAccion.PENDING);
        }

        @Test
        @DisplayName("preserves aiConversacionId for audit linkage")
        void crear_preservaAiConversacionId() {
            AiAccion p = AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, "wa-msg-9", AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, AHORA
            );
            assertThat(p.getAiConversacionId()).isEqualTo(AI_CONVERSACION_ID);
            assertThat(p.getWaMensajeId()).isEqualTo("wa-msg-9");
        }

        @Test
        @DisplayName("accepts null waMensajeId when not triggered by a single message")
        void crear_waMensajeIdNulo_acepta() {
            AiAccion p = AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, AHORA
            );
            assertThat(p.getWaMensajeId()).isNull();
        }

        @Test
        @DisplayName("throws when empresaId is null")
        void crear_conEmpresaIdNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiAccion.crear(
                    null, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("empresaId");
        }

        @Test
        @DisplayName("throws when solicitadaPor is null")
        void crear_conSolicitanteNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiAccion.crear(
                    EMPRESA, null, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("solicitadaPor");
        }

        @Test
        @DisplayName("throws when aiConversacionId is null")
        void crear_conAiConversacionIdNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, null,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("aiConversacionId");
        }

        @Test
        @DisplayName("throws when payloadJson is blank")
        void crear_conPayloadVacio_lanzaExcepcion() {
            assertThatThrownBy(() -> AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, "", RATIONALE, 60, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("payloadJson");
        }

        @Test
        @DisplayName("throws when ttl is not positive")
        void crear_conTtlInvalido_lanzaExcepcion() {
            assertThatThrownBy(() -> AiAccion.crear(
                    EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 0, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("ttl");
        }
    }

    // ── State machine ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("State machine")
    class MaquinaDeEstados {

        @Test
        @DisplayName("confirmar() moves PENDING → CONFIRMED and increments version")
        void confirmar_transicionValida_incrementaVersion() {
            AiAccion p = nuevaAccion(AHORA);
            AiAccion confirmada = p.confirmar(AHORA.plusMinutes(1));

            assertThat(confirmada.getEstado()).isEqualTo(EstadoAccion.CONFIRMED);
            assertThat(confirmada.getVersion()).isEqualTo(p.getVersion() + 1);
        }

        @Test
        @DisplayName("rechazar() moves PENDING → REJECTED and increments version")
        void rechazar_transicionValida_incrementaVersion() {
            AiAccion p = nuevaAccion(AHORA);
            AiAccion rechazada = p.rechazar(AHORA.plusMinutes(1));

            assertThat(rechazada.getEstado()).isEqualTo(EstadoAccion.REJECTED);
            assertThat(rechazada.getVersion()).isEqualTo(p.getVersion() + 1);
        }

        @Test
        @DisplayName("expirar() moves PENDING → EXPIRED; idempotent on EXPIRED")
        void expirar_transicionValida_yEsIdempotente() {
            AiAccion p = nuevaAccion(AHORA);
            AiAccion expirada = p.expirar(AHORA.plusHours(2));
            assertThat(expirada.getEstado()).isEqualTo(EstadoAccion.EXPIRED);
            assertThat(expirada.expirar(AHORA.plusHours(3))).isSameAs(expirada);
        }

        @Test
        @DisplayName("marcarEjecutada() moves CONFIRMED → EXECUTED with entity id")
        void marcarEjecutada_confirmadaPasaAExecuted() {
            AiAccion confirmada = nuevaAccion(AHORA).confirmar(AHORA.plusMinutes(1));
            AiAccion ejecutada = confirmada.marcarEjecutada("tarea-99", AHORA.plusMinutes(2));

            assertThat(ejecutada.getEstado()).isEqualTo(EstadoAccion.EXECUTED);
            assertThat(ejecutada.getResultadoEntidadId()).isEqualTo("tarea-99");
        }

        @Test
        @DisplayName("marcarFallida() moves CONFIRMED → FAILED with error reason")
        void marcarFallida_confirmadaPasaAFailed() {
            AiAccion confirmada = nuevaAccion(AHORA).confirmar(AHORA.plusMinutes(1));
            AiAccion fallida = confirmada.marcarFallida("Contacto no encontrado", AHORA.plusMinutes(2));

            assertThat(fallida.getEstado()).isEqualTo(EstadoAccion.FAILED);
            assertThat(fallida.getErrorReason()).isEqualTo("Contacto no encontrado");
        }

        @Test
        @DisplayName("confirmar() rejects any non-PENDING estado")
        void confirmar_enEstadoNoPending_lanzaExcepcion() {
            AiAccion rechazada = nuevaAccion(AHORA).rechazar(AHORA.plusMinutes(1));

            assertThatThrownBy(() -> rechazada.confirmar(AHORA.plusMinutes(2)))
                    .isInstanceOf(AccionStateTransitionException.class)
                    .hasMessageContaining("REJECTED");
        }

        @Test
        @DisplayName("marcarEjecutada() rejects PENDING without CONFIRMED first")
        void marcarEjecutada_enPending_lanzaExcepcion() {
            AiAccion p = nuevaAccion(AHORA);

            assertThatThrownBy(() -> p.marcarEjecutada("cualquiera", AHORA.plusMinutes(1)))
                    .isInstanceOf(AccionStateTransitionException.class);
        }

        @Test
        @DisplayName("esTerminal() reports terminal states correctly")
        void esTerminal_soloEstadosTerminales() {
            AiAccion p = nuevaAccion(AHORA);
            assertThat(p.esTerminal()).isFalse();

            assertThat(p.rechazar(AHORA.plusMinutes(1)).esTerminal()).isTrue();
            assertThat(p.expirar(AHORA.plusMinutes(1)).esTerminal()).isTrue();

            AiAccion c = p.confirmar(AHORA.plusMinutes(1));
            assertThat(c.esTerminal()).isFalse();
            assertThat(c.marcarEjecutada("x", AHORA.plusMinutes(2)).esTerminal()).isTrue();
            assertThat(c.marcarFallida("boom", AHORA.plusMinutes(2)).esTerminal()).isTrue();
        }
    }

    // ── Ownership and expiry ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Ownership and expiry queries")
    class OwnershipYExpiracion {

        @Test
        @DisplayName("perteneceA(UsuarioId) is true only for the requester")
        void perteneceA_usuario_trueSoloSolicitante() {
            AiAccion p = nuevaAccion(AHORA);
            assertThat(p.perteneceA(SOLICITANTE)).isTrue();
            assertThat(p.perteneceA(OTRO_USUARIO)).isFalse();
        }

        @Test
        @DisplayName("perteneceA(EmpresaId) is true only for the owning company")
        void perteneceA_empresa_trueSoloEmpresaCorrecta() {
            AiAccion p = nuevaAccion(AHORA);
            assertThat(p.perteneceA(EMPRESA)).isTrue();
            assertThat(p.perteneceA(EmpresaId.create())).isFalse();
        }

        @Test
        @DisplayName("estaExpirada is false for PENDING within ttl window")
        void estaExpirada_falseDentroDeVentana() {
            AiAccion p = nuevaAccion(AHORA);
            assertThat(p.estaExpirada(AHORA.plusMinutes(30))).isFalse();
        }

        @Test
        @DisplayName("estaExpirada is true for PENDING past ttl window")
        void estaExpirada_trueFueraDeVentana() {
            AiAccion p = nuevaAccion(AHORA);
            assertThat(p.estaExpirada(AHORA.plusHours(2))).isTrue();
        }

        @Test
        @DisplayName("estaExpirada is false for terminal states even past ttl")
        void estaExpirada_falseEnEstadosTerminales() {
            AiAccion rechazada = nuevaAccion(AHORA).rechazar(AHORA.plusMinutes(1));
            assertThat(rechazada.estaExpirada(AHORA.plusHours(2))).isFalse();
        }
    }

    // ── reconstitute() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("preserves id, version and estado from persistence")
        void reconstituir_preservaEstadoYVersion() {
            AiAccionId id = AiAccionId.create();

            AiAccion p = AiAccion.reconstitute(
                    id, EMPRESA, SOLICITANTE, WA_CONVERSACION_ID,
                    "wa-msg-99", AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE,
                    3, AHORA.plusHours(1),
                    "entidad-1", null,
                    EstadoAccion.CONFIRMED,
                    AHORA.minusDays(1), AHORA
            );

            assertThat(p.getId()).isEqualTo(id);
            assertThat(p.getVersion()).isEqualTo(3);
            assertThat(p.getEstado()).isEqualTo(EstadoAccion.CONFIRMED);
            assertThat(p.getAiConversacionId()).isEqualTo(AI_CONVERSACION_ID);
            assertThat(p.getWaMensajeId()).isEqualTo("wa-msg-99");
        }

        @Test
        @DisplayName("preserves null audit-link fields when missing in persistence")
        void reconstituir_auditLinksNulos_preserva() {
            AiAccionId id = AiAccionId.create();
            AiAccion p = AiAccion.reconstitute(
                    id, EMPRESA, SOLICITANTE, WA_CONVERSACION_ID,
                    null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE,
                    1, AHORA.plusHours(1),
                    null, null,
                    EstadoAccion.PENDING,
                    AHORA, AHORA
            );
            assertThat(p.getWaMensajeId()).isNull();
        }

        @Test
        @DisplayName("throws when id is null")
        void reconstituir_conIdNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiAccion.reconstitute(
                    null, EMPRESA, SOLICITANTE, WA_CONVERSACION_ID,
                    null, AI_CONVERSACION_ID,
                    TIPO_ACCION, PAYLOAD_JSON, RATIONALE,
                    1, AHORA.plusHours(1),
                    null, null,
                    EstadoAccion.PENDING,
                    AHORA, AHORA
            )).isInstanceOf(InvariantViolationException.class);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static AiAccion nuevaAccion(LocalDateTime ahora) {
        return AiAccion.crear(
                EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null, AI_CONVERSACION_ID,
                TIPO_ACCION, PAYLOAD_JSON, RATIONALE, 60, ahora
        );
    }
}