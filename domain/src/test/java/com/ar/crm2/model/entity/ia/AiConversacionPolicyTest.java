package com.ar.crm2.model.entity.ia;

import com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AiConversacion#requireOwnedBy(UsuarioId, EmpresaId)}.
 *
 * <p>Encapsulates the ownership + tenant check that used to live
 * inline in {@code ObtenerConversacionAsistenteService} and
 * {@code RegistrarMensajeAsistenteService}.
 */
class AiConversacionPolicyTest {

    private static final UsuarioId ACTOR = UsuarioId.create();
    private static final UsuarioId OTRO = UsuarioId.create();
    private static final EmpresaId EMPRESA = EmpresaId.create();
    private static final EmpresaId OTRA_EMPRESA = EmpresaId.create();
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 6, 24, 12, 0);

    @Nested
    @DisplayName("requireOwnedBy(actor, empresa)")
    class RequireOwnedBy {

        @Test
        @DisplayName("passes when both actor and empresa match")
        void requireOwnedBy_actorYEmpresaCorrectos_noLanza() {
            AiConversacion c = AiConversacion.crear(EMPRESA, ACTOR, "wa-1", null, AHORA);
            assertThatCode(() -> c.requireOwnedBy(ACTOR, EMPRESA))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when the actor is different")
        void requireOwnedBy_actorDistinto_lanzaExcepcion() {
            AiConversacion c = AiConversacion.crear(EMPRESA, ACTOR, "wa-1", null, AHORA);
            assertThatThrownBy(() -> c.requireOwnedBy(OTRO, EMPRESA))
                    .isInstanceOf(ConversacionAsistenteNotOwnedByActorException.class)
                    .hasMessageContaining(OTRO.value().toString())
                    .hasMessageContaining(c.getId().value().toString());
        }

        @Test
        @DisplayName("throws when the empresa is different")
        void requireOwnedBy_empresaDistinta_lanzaExcepcion() {
            AiConversacion c = AiConversacion.crear(EMPRESA, ACTOR, "wa-1", null, AHORA);
            assertThatThrownBy(() -> c.requireOwnedBy(ACTOR, OTRA_EMPRESA))
                    .isInstanceOf(ConversacionAsistenteNotOwnedByActorException.class)
                    .hasMessageContaining(c.getId().value().toString());
        }

        @Test
        @DisplayName("checks actor identity before empresa")
        void requireOwnedBy_actorYEmpresaDistintos_reportaElActor() {
            AiConversacion c = AiConversacion.crear(EMPRESA, ACTOR, "wa-1", null, AHORA);
            assertThatThrownBy(() -> c.requireOwnedBy(OTRO, OTRA_EMPRESA))
                    .isInstanceOf(ConversacionAsistenteNotOwnedByActorException.class)
                    .hasMessageContaining(OTRO.value().toString());
        }

        @Test
        @DisplayName("accepts a conversation with a non-null contactoId (does not affect ownership)")
        void requireOwnedBy_conContactoId_noLanza() {
            AiConversacion c = AiConversacion.crear(
                    EMPRESA, ACTOR, "wa-1", ContactoId.create(), AHORA
            );
            assertThatCode(() -> c.requireOwnedBy(ACTOR, EMPRESA))
                    .doesNotThrowAnyException();
        }
    }
}