package com.ar.crm2.adapter.in.rest.ai;

import com.ar.crm2.adapter.in.rest.dto.ai.AnalizarChatRequest;
import com.ar.crm2.adapter.in.rest.dto.ai.ListarAccionesPendientesRequest;
import com.ar.crm2.application.ai.command.AnalizarChatCommand;
import com.ar.crm2.application.ai.command.ConfirmarAccionCommand;
import com.ar.crm2.application.ai.command.ListarAccionesPendientesCommand;
import com.ar.crm2.application.ai.command.ObtenerConversacionAsistenteCommand;
import com.ar.crm2.application.ai.command.RechazarAccionCommand;
import com.ar.crm2.application.security.ActorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AiRestCommandMapper}.
 *
 * <p>The mapper is the single boundary that translates HTTP-layer
 * concerns (path variables, query parameters, JSON DTOs) into the
 * application command records. The controller does NOT instantiate
 * commands directly; every inbound command is built through one of
 * the command-specific methods on this mapper.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Identity anchor sourced from the trusted {@link ActorContext}
 *       — never from the request body.</li>
 *   <li>{@code /chat} always carries {@code empresaId = null}
 *       (resource-first tenant model).</li>
 *   <li>Resource-bound endpoints forward the {@code empresaId} query
 *       parameter verbatim.</li>
 *   <li>Path variables and query parameters land in the matching
 *       command record fields.</li>
 * </ul>
 */
class AiRestCommandMapperTest {

    private static final UUID ACTOR_USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EMPRESA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private ActorContext actor() {
        return new ActorContext(
                "test-subject",
                "test-user",
                "test@example.com",
                Optional.of(ACTOR_USUARIO_ID),
                Optional.empty(),
                Set.of("USER")
        );
    }

    // ── POST /api/ai/chat ──────────────────────────────────────────

    @Test
    @DisplayName("/chat: actor.usuarioId es el anchor; empresaId siempre null (resource-first)")
    void chat_resourceFirst_empresaIdSiempreNull() {
        AnalizarChatCommand cmd = AiRestCommandMapper.toCommand(
                actor(),
                new AnalizarChatRequest("wa-conv-1", "hola")
        );

        assertEquals(ACTOR_USUARIO_ID, cmd.actorUsuarioId());
        assertNull(cmd.empresaId(),
                "/chat MUST NOT forward any tenant hint — resource-first tenant model");
        assertEquals("wa-conv-1", cmd.waConversacionId());
        assertEquals("hola", cmd.mensajeUsuario());
    }

    @Test
    @DisplayName("/chat: mensajeUsuario null/blank representa first-turn sin pregunta")
    void chat_mensajeUsuarioNullRepresentaFirstTurn() {
        AnalizarChatCommand cmd = AiRestCommandMapper.toCommand(
                actor(),
                new AnalizarChatRequest("wa-conv-1", null)
        );

        assertEquals(ACTOR_USUARIO_ID, cmd.actorUsuarioId());
        assertNull(cmd.empresaId());
        assertEquals("wa-conv-1", cmd.waConversacionId());
        assertNull(cmd.mensajeUsuario(),
                "mensajeUsuario null es válido — representa first-turn sin kick-off question");
    }

    @Test
    @DisplayName("/chat: actor sin usuarioId es rechazado por el comando")
    void chat_actorSinUsuarioIdRechazadoPorCommand() {
        ActorContext anonActor = new ActorContext(
                "anon-subject", "anon", null, Optional.empty(), Optional.empty(), Set.of()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AiRestCommandMapper.toCommand(
                        anonActor,
                        new AnalizarChatRequest("wa-conv-1", "x")
                ));

        assertEquals("actorUsuarioId is required", ex.getMessage());
    }

    // ── POST /api/ai/acciones/{id}/confirmar ──────────────────────

    @Test
    @DisplayName("confirmar: propaga accionId + expectedVersion + empresaId y sella actor desde ActorContext")
    void confirmar_propagaPathYQueryYActor() {
        UUID accionId = UUID.randomUUID();
        ConfirmarAccionCommand cmd = AiRestCommandMapper.toCommand(
                actor(), accionId, 3, EMPRESA_ID
        );

        assertEquals(ACTOR_USUARIO_ID, cmd.actorUsuarioId());
        assertEquals(accionId, cmd.accionId());
        assertEquals(3, cmd.expectedVersion());
        assertEquals(EMPRESA_ID, cmd.empresaId());
    }

    // ── POST /api/ai/acciones/{id}/rechazar ───────────────────────

    @Test
    @DisplayName("rechazar: propaga accionId + empresaId y sella actor desde ActorContext")
    void rechazar_propagaPathYQueryYActor() {
        UUID accionId = UUID.randomUUID();
        RechazarAccionCommand cmd = AiRestCommandMapper.toRechazarAccionCommand(
                actor(), accionId, EMPRESA_ID
        );

        assertEquals(ACTOR_USUARIO_ID, cmd.actorUsuarioId());
        assertEquals(accionId, cmd.accionId());
        assertEquals(EMPRESA_ID, cmd.empresaId());
    }

    // ── GET /api/ai/conversaciones/{id} ───────────────────────────

    @Test
    @DisplayName("obtener conversacion: propaga aiConversacionId + empresaId y sella actor")
    void obtenerConversacion_propagaPathYQueryYActor() {
        UUID aiConvId = UUID.randomUUID();
        ObtenerConversacionAsistenteCommand cmd = AiRestCommandMapper.toObtenerConversacionCommand(
                actor(), aiConvId, EMPRESA_ID
        );

        assertEquals(ACTOR_USUARIO_ID, cmd.actorUsuarioId());
        assertEquals(aiConvId, cmd.aiConversacionId());
        assertEquals(EMPRESA_ID, cmd.empresaId());
    }

    // ── GET /api/ai/acciones ───────────────────────────────────────

    @Test
    @DisplayName("listar acciones: DTO + actor propaga empresaId + limite y sella actor")
    void listarAcciones_desdeDto_propagaEmpresaYLimite() {
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                EMPRESA_ID, 25
        );

        ListarAccionesPendientesCommand cmd = AiRestCommandMapper.toListarAccionesPendientesCommand(
                actor(), request
        );

        assertEquals(ACTOR_USUARIO_ID, cmd.actorUsuarioId());
        assertEquals(EMPRESA_ID, cmd.empresaId());
        assertEquals(25, cmd.limite());
    }

    @Test
    @DisplayName("listar acciones: limite null en el DTO cae al default del mapper (50)")
    void listarAcciones_limiteNull_enDto_caeADefaultMapper() {
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                EMPRESA_ID, null
        );

        ListarAccionesPendientesCommand cmd = AiRestCommandMapper.toListarAccionesPendientesCommand(
                actor(), request
        );

        assertEquals(EMPRESA_ID, cmd.empresaId());
        assertEquals(50, cmd.limite(),
                "limite null en el DTO MUST default to 50 (controller-default-equivalent in mapper)");
    }

    @Test
    @DisplayName("listar acciones: empresaId null en el DTO es rechazado por el command")
    void listarAcciones_empresaIdNull_enDto_rechazadoPorCommand() {
        // The DTO @NotNull validation in the controller produces a 400,
        // but the mapper is a defense-in-depth boundary that also lets
        // the IllegalArgumentException surface from the canonical
        // constructor if any alternate path (tests, future direct callers)
        // bypasses the controller validation.
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                null, 50
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AiRestCommandMapper.toListarAccionesPendientesCommand(actor(), request));

        assertEquals("empresaId is required", ex.getMessage());
    }

    @Test
    @DisplayName("listar acciones: limite fuera del rango es rechazado por el command")
    void listarAcciones_limiteFueraDeRango_rechazadoPorCommand() {
        // The DTO @Min/@Max validation in the controller produces a 400,
        // but the mapper delegates to the command which enforces the same
        // invariant via its canonical constructor.
        ListarAccionesPendientesRequest request = new ListarAccionesPendientesRequest(
                EMPRESA_ID, 9999
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AiRestCommandMapper.toListarAccionesPendientesCommand(actor(), request));

        assertEquals("limite must be between 1 and 200", ex.getMessage());
    }
}
