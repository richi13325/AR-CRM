package com.ar.crm2.model.entity;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.vo.AiConversacionId;
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
 * Unit tests for {@link AiConversacion} domain behavior.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Identity + scope (empresaId, actorUsuarioId, waConversacionId) preserved.</li>
 *   <li>Ownership queries (perteneceA for UsuarioId and EmpresaId).</li>
 *   <li>Scope query: scopeEs() matches only the source WhatsApp conversation.</li>
 *   <li>archivar() is idempotent and bumps actualizadoEn.</li>
 *   <li>reconstitute() preserves state from persistence.</li>
 * </ul>
 */
class AiConversacionTest {

    private static final EmpresaId EMPRESA = EmpresaId.create();
    private static final UsuarioId ACTOR = UsuarioId.create();
    private static final UsuarioId OTRO_USUARIO = UsuarioId.create();
    private static final ContactoId CONTACTO = ContactoId.create();
    private static final String WA_CONVERSACION_ID = "wa-conv-7";
    private static final LocalDateTime AHORA = LocalDateTime.now();

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("succeeds with required fields; contactoId optional")
        void crear_conCamposRequeridos_exitoso() {
            assertThatCode(() -> AiConversacion.crear(
                    EMPRESA, ACTOR, WA_CONVERSACION_ID, null, AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("succeeds when contactoId is provided")
        void crear_conContactoId_exitoso() {
            assertThatCode(() -> AiConversacion.crear(
                    EMPRESA, ACTOR, WA_CONVERSACION_ID, CONTACTO, AHORA
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("starts non-archived with version-stable timestamps")
        void crear_arrancaNoArchivada() {
            AiConversacion c = AiConversacion.crear(
                    EMPRESA, ACTOR, WA_CONVERSACION_ID, CONTACTO, AHORA
            );
            assertThat(c.isArchivada()).isFalse();
            assertThat(c.getCreadoEn()).isEqualTo(AHORA);
            assertThat(c.getActualizadoEn()).isEqualTo(AHORA);
        }

        @Test
        @DisplayName("throws when empresaId is null")
        void crear_conEmpresaIdNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiConversacion.crear(
                    null, ACTOR, WA_CONVERSACION_ID, null, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("empresaId");
        }

        @Test
        @DisplayName("throws when waConversacionId is blank")
        void crear_conWaConversacionIdBlanco_lanzaExcepcion() {
            assertThatThrownBy(() -> AiConversacion.crear(
                    EMPRESA, ACTOR, "  ", null, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("waConversacionId");
        }
    }

    @Nested
    @DisplayName("Ownership and scope queries")
    class OwnershipYScope {

        @Test
        @DisplayName("perteneceA(UsuarioId) true only for the actor")
        void perteneceA_usuario_trueSoloActor() {
            AiConversacion c = nuevaConversacion(AHORA);
            assertThat(c.perteneceA(ACTOR)).isTrue();
            assertThat(c.perteneceA(OTRO_USUARIO)).isFalse();
        }

        @Test
        @DisplayName("perteneceA(EmpresaId) true only for the owning company")
        void perteneceA_empresa_trueSoloEmpresaCorrecta() {
            AiConversacion c = nuevaConversacion(AHORA);
            assertThat(c.perteneceA(EMPRESA)).isTrue();
            assertThat(c.perteneceA(EmpresaId.create())).isFalse();
        }

        @Test
        @DisplayName("scopeEs(waConversacionId) true only for the source WhatsApp conversation")
        void scopeEs_trueSoloConversacionOrigen() {
            AiConversacion c = nuevaConversacion(AHORA);
            assertThat(c.scopeEs(WA_CONVERSACION_ID)).isTrue();
            assertThat(c.scopeEs("otra-conversacion")).isFalse();
        }
    }

    @Nested
    @DisplayName("archivar()")
    class Archivar {

        @Test
        @DisplayName("moves to archived; idempotent on already-archived")
        void archivar_idempotente() {
            AiConversacion c = nuevaConversacion(AHORA);
            AiConversacion archivada = c.archivar(AHORA.plusMinutes(5));
            assertThat(archivada.isArchivada()).isTrue();
            assertThat(archivada.getActualizadoEn()).isEqualTo(AHORA.plusMinutes(5));
            // idempotent
            assertThat(archivada.archivar(AHORA.plusMinutes(10))).isSameAs(archivada);
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstituir {

        @Test
        @DisplayName("preserves id, archivada flag and timestamps")
        void reconstituir_preservaEstadoCompleto() {
            AiConversacionId id = AiConversacionId.create();
            AiConversacion c = AiConversacion.reconstitute(
                    id, EMPRESA, ACTOR, WA_CONVERSACION_ID, CONTACTO,
                    true, AHORA.minusDays(2), AHORA.minusHours(1)
            );
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.isArchivada()).isTrue();
            assertThat(c.getCreadoEn()).isEqualTo(AHORA.minusDays(2));
            assertThat(c.getActualizadoEn()).isEqualTo(AHORA.minusHours(1));
        }

        @Test
        @DisplayName("throws when id is null")
        void reconstituir_conIdNulo_lanzaExcepcion() {
            assertThatThrownBy(() -> AiConversacion.reconstitute(
                    null, EMPRESA, ACTOR, WA_CONVERSACION_ID, CONTACTO,
                    false, AHORA, AHORA
            )).isInstanceOf(InvariantViolationException.class)
              .hasMessageContaining("id");
        }
    }

    // ── helpers ────────────────────────────────────────────────────

    private static AiConversacion nuevaConversacion(LocalDateTime ahora) {
        return AiConversacion.crear(EMPRESA, ACTOR, WA_CONVERSACION_ID, CONTACTO, ahora);
    }
}