package com.ar.crm2.model.entity.ia;

import com.ar.crm2.exception.AccionExpiredException;
import com.ar.crm2.exception.AccionNotOwnedByActorException;
import com.ar.crm2.exception.AccionStateException;
import com.ar.crm2.exception.AccionVersionMismatchException;
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
 * Unit tests for {@link AiAccion} policy methods.
 *
 * <p>These methods encapsulate the ownership / tenant / state /
 * version / expiry checks that used to live inline in the AI
 * application services. Moving the policy into the aggregate keeps
 * the application layer as a thin coordinator and gives every check
 * a single, testable source of truth.
 */
class AiAccionPolicyTest {

    private static final UsuarioId SOLICITANTE = UsuarioId.create();
    private static final UsuarioId OTRO_USUARIO = UsuarioId.create();
    private static final EmpresaId EMPRESA = EmpresaId.create();
    private static final EmpresaId OTRA_EMPRESA = EmpresaId.create();
    private static final AiConversacionId AI_CONVERSACION_ID = AiConversacionId.create();
    private static final String WA_CONVERSACION_ID = "wa-conv-123";
    private static final String TIPO_ACCION = "CREATE_TAREA";
    private static final String PAYLOAD_JSON = "{\"titulo\":\"x\"}";
    private static final String RATIONALE = "r";
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 6, 24, 12, 0);

    // ── requireOwnedBy() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("requireOwnedBy(actor, empresa)")
    class RequireOwnedBy {

        @Test
        @DisplayName("passes when both the actor and the empresa match")
        void requireOwnedBy_actorYEmpresaCorrectos_noLanza() {
            AiAccion p = nuevaAccion(AHORA);
            assertThatCode(() -> p.requireOwnedBy(SOLICITANTE, EMPRESA))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws AccionNotOwnedByActorException when the actor is different")
        void requireOwnedBy_actorDistinto_lanzaExcepcion() {
            AiAccion p = nuevaAccion(AHORA);
            assertThatThrownBy(() -> p.requireOwnedBy(OTRO_USUARIO, EMPRESA))
                    .isInstanceOf(AccionNotOwnedByActorException.class)
                    .hasMessageContaining(OTRO_USUARIO.value().toString())
                    .hasMessageContaining(p.getId().value().toString());
        }

        @Test
        @DisplayName("throws AccionNotOwnedByActorException when the empresa is different")
        void requireOwnedBy_empresaDistinta_lanzaExcepcion() {
            AiAccion p = nuevaAccion(AHORA);
            assertThatThrownBy(() -> p.requireOwnedBy(SOLICITANTE, OTRA_EMPRESA))
                    .isInstanceOf(AccionNotOwnedByActorException.class)
                    .hasMessageContaining(p.getId().value().toString());
        }

        @Test
        @DisplayName("checks actor identity BEFORE empresa (cheaper failure path)")
        void requireOwnedBy_actorYEmpresaDistintos_reportaSoloElActor() {
            AiAccion p = nuevaAccion(AHORA);
            assertThatThrownBy(() -> p.requireOwnedBy(OTRO_USUARIO, OTRA_EMPRESA))
                    .isInstanceOf(AccionNotOwnedByActorException.class)
                    .hasMessageContaining(OTRO_USUARIO.value().toString())
                    .satisfies(ex -> assertThat(ex.getMessage())
                            .doesNotContain(OTRA_EMPRESA.value().toString()));
        }
    }

    // ── requirePending() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("requirePending(operacion)")
    class RequirePending {

        @Test
        @DisplayName("passes when estado is PENDING")
        void requirePending_estadoPending_noLanza() {
            AiAccion p = nuevaAccion(AHORA);
            assertThatCode(() -> p.requirePending("rechazar")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws AccionStateException when estado is REJECTED")
        void requirePending_estadoRejected_lanzaExcepcion() {
            AiAccion rechazada = nuevaAccion(AHORA).rechazar(AHORA.plusMinutes(1));
            assertThatThrownBy(() -> rechazada.requirePending("rechazar"))
                    .isInstanceOf(AccionStateException.class)
                    .hasMessageContaining("REJECTED")
                    .hasMessageContaining("rechazar");
        }

        @Test
        @DisplayName("throws AccionStateException when estado is CONFIRMED")
        void requirePending_estadoConfirmed_lanzaExcepcion() {
            AiAccion confirmada = nuevaAccion(AHORA).confirmar(AHORA.plusMinutes(1));
            assertThatThrownBy(() -> confirmada.requirePending("rechazar"))
                    .isInstanceOf(AccionStateException.class)
                    .hasMessageContaining("CONFIRMED");
        }

        @Test
        @DisplayName("throws AccionStateException when estado is EXPIRED")
        void requirePending_estadoExpired_lanzaExcepcion() {
            AiAccion expirada = AiAccion.reconstitute(
                    AiAccionId.create(), EMPRESA, SOLICITANTE, WA_CONVERSACION_ID, null,
                    AI_CONVERSACION_ID, TIPO_ACCION, PAYLOAD_JSON, RATIONALE,
                    2, AHORA.plusHours(1), null, null,
                    EstadoAccion.EXPIRED, AHORA, AHORA.plusMinutes(1)
            );
            assertThatThrownBy(() -> expirada.requirePending("rechazar"))
                    .isInstanceOf(AccionStateException.class)
                    .hasMessageContaining("EXPIRED");
        }
    }

    // ── requireVersion() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("requireVersion(expected)")
    class RequireVersion {

        @Test
        @DisplayName("passes when version matches expectedVersion")
        void requireVersion_versionCoincide_noLanza() {
            AiAccion p = nuevaAccion(AHORA); // version=1
            assertThatCode(() -> p.requireVersion(1)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws AccionVersionMismatchException when version differs")
        void requireVersion_versionDistinta_lanzaExcepcion() {
            AiAccion p = nuevaAccion(AHORA); // version=1
            assertThatThrownBy(() -> p.requireVersion(99))
                    .isInstanceOf(AccionVersionMismatchException.class)
                    .hasMessageContaining("99")
                    .hasMessageContaining("1");
        }
    }

    // ── requireNotExpired() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("requireNotExpired(ahora)")
    class RequireNotExpired {

        @Test
        @DisplayName("passes for PENDING within ttl window")
        void requireNotExpired_dentroDeVentana_noLanza() {
            AiAccion p = nuevaAccion(AHORA);
            assertThatCode(() -> p.requireNotExpired(AHORA.plusMinutes(30)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws AccionExpiredException for PENDING past ttl window")
        void requireNotExpired_fueraDeVentana_lanzaExcepcion() {
            AiAccion p = nuevaAccion(AHORA); // ttl 60min
            assertThatThrownBy(() -> p.requireNotExpired(AHORA.plusHours(2)))
                    .isInstanceOf(AccionExpiredException.class)
                    .hasMessageContaining(p.getId().value().toString());
        }

        @Test
        @DisplayName("passes for terminal states even past ttl")
        void requireNotExpired_estadosTerminales_noLanza() {
            AiAccion rechazada = nuevaAccion(AHORA).rechazar(AHORA.plusMinutes(1));
            assertThatCode(() -> rechazada.requireNotExpired(AHORA.plusHours(2)))
                    .doesNotThrowAnyException();
        }
    }

    // ── requireConfirmable() ─────────────────────────────────────────────────

    @Nested
    @DisplayName("requireConfirmable(expectedVersion, ahora)")
    class RequireConfirmable {

        @Test
        @DisplayName("passes and returns this when all checks succeed")
        void requireConfirmable_todoValido_retornaThis() {
            AiAccion p = nuevaAccion(AHORA); // PENDING, v1, fresh ttl
            AiAccion result = p.requireConfirmable(1, AHORA.plusMinutes(5));
            assertThat(result).isSameAs(p);
        }

        @Test
        @DisplayName("rejects CONFIRMED state with AccionStateException")
        void requireConfirmable_confirmada_lanzaState() {
            AiAccion confirmada = nuevaAccion(AHORA).confirmar(AHORA.plusMinutes(1));
            assertThatThrownBy(() -> confirmada.requireConfirmable(2, AHORA.plusMinutes(2)))
                    .isInstanceOf(AccionStateException.class)
                    .hasMessageContaining("CONFIRMED");
        }

        @Test
        @DisplayName("rejects version mismatch with AccionVersionMismatchException")
        void requireConfirmable_versionIncorrecta_lanzaVersion() {
            AiAccion p = nuevaAccion(AHORA); // v1
            assertThatThrownBy(() -> p.requireConfirmable(7, AHORA.plusMinutes(5)))
                    .isInstanceOf(AccionVersionMismatchException.class);
        }

        @Test
        @DisplayName("rejects expired PENDING with AccionExpiredException")
        void requireConfirmable_expirada_lanzaExpired() {
            AiAccion p = nuevaAccion(AHORA); // ttl 60min
            assertThatThrownBy(() -> p.requireConfirmable(1, AHORA.plusHours(2)))
                    .isInstanceOf(AccionExpiredException.class);
        }

        @Test
        @DisplayName("state is checked before version (order matters)")
        void requireConfirmable_estadoIncorrectoYVersionIncorrecta_reportaEstado() {
            AiAccion rechazada = nuevaAccion(AHORA).rechazar(AHORA.plusMinutes(1)); // REJECTED, v2
            assertThatThrownBy(() -> rechazada.requireConfirmable(99, AHORA.plusMinutes(5)))
                    .isInstanceOf(AccionStateException.class);
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