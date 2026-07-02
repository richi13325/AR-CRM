package com.ar.crm2.adapter.in.rest.ai;

import com.ar.crm2.adapter.in.rest.GlobalExceptionHandler;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.application.ai.port.in.AnalizarChatUseCase;
import com.ar.crm2.application.ai.port.in.ConfirmarAccionUseCase;
import com.ar.crm2.application.ai.port.in.ListarAccionesPendientesUseCase;
import com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase;
import com.ar.crm2.application.ai.port.in.RechazarAccionUseCase;
import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;
import com.ar.crm2.application.ai.port.in.result.ResultadoEjecucionAccion;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.exception.AccionStateException;
import com.ar.crm2.exception.AccionVersionMismatchException;
import com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException;
import com.ar.crm2.security.KeycloakJwtActorContextMapper;
import com.ar.crm2.security.WaProperties;
import com.ar.crm2.whatsapp.application.bot.port.in.FindBotByTokenUseCase;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiMensajeId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AI assistant REST controller (PR 4 slice).
 *
 * <p>Mirrors the {@link com.ar.crm2.adapter.in.rest.TableroControllerIT}
 * pattern: a {@code @WebMvcTest} slice that loads only the web layer,
 * mocks every inbound use case, and asserts the HTTP contract end-to-end.
 *
 * <p>Scenarios covered by the PR 4 spec:
 * <ul>
 *   <li><b>Happy — chat</b>: {@code POST /api/ai/chat} returns the AI
 *       conversation id and the assistant text.</li>
 *   <li><b>Happy — confirm</b>: {@code POST /api/ai/acciones/{id}/confirmar}
 *       returns the executed/result entity id and the new version.</li>
 *   <li><b>Happy — list</b>: {@code GET /api/ai/acciones} returns the
 *       requester's pending AI conversations list (the controller
 *       surfaces {@code ListarConversacionesAsistenteUseCase} because
 *       the design reuses the conversation-listing use case for the
 *       simple "my pending actions" view).</li>
 *   <li><b>Happy — get conversation</b>: {@code GET /api/ai/conversaciones/{id}}
 *       returns the conversation + its ordered message history.</li>
 *   <li><b>Tenant leak — confirm</b>: a user from a different
 *       {@code empresaId} cannot confirm an action they do not own;
 *       the use case throws {@link ConversacionAsistenteNotOwnedByActorException}
 *       and the controller must surface it as 403.</li>
 *   <li><b>Replay — confirm</b>: confirming an already-confirmed action
 *       raises {@link AccionStateException} (or
 *       {@link AccionVersionMismatchException}) and the controller
 *       surfaces it as 409.</li>
 * </ul>
 *
 * <p>All use cases are mocked with {@link MockitoBean} because they
 * belong to the application layer (and the web slice does not load
 * it). The Keycloak mapper is mocked too because
 * {@link com.ar.crm2.security.ActorContextRequestAttributeFilter} is
 * a {@code @Component} that constructor-injects it; without the
 * override the MVC slice fails to bootstrap.
 */
@WebMvcTest(controllers = {AiController.class, GlobalExceptionHandler.class})
@WithMockUser
class AiControllerIT {

    // The {@code ActorContextRequestAttributeFilter} injects a dev
    // context with this id whenever the principal is not a Jwt
    // (i.e. under {@code @WithMockUser}). The tests target that flow.
    private static final UUID ACTOR_USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EMPRESA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalizarChatUseCase analizarChatUseCase;

    @MockitoBean
    private ConfirmarAccionUseCase confirmarAccionUseCase;

    @MockitoBean
    private RechazarAccionUseCase rechazarAccionUseCase;

    @MockitoBean
    private ObtenerConversacionAsistenteUseCase obtenerConversacionAsistenteUseCase;

    @MockitoBean
    private ListarAccionesPendientesUseCase listarAccionesPendientesUseCase;

    @MockitoBean
    private KeycloakJwtActorContextMapper actorContextMapper;

    @MockitoBean
    private FindBotByTokenUseCase findBotByTokenUseCase;

    @MockitoBean
    private WaProperties waProperties;

    @BeforeEach
    void stubActorContextMapper() {
        ActorContext actorContext = new ActorContext(
                "test-subject",
                "test-user",
                "test@example.com",
                Optional.of(ACTOR_USUARIO_ID),
                Optional.empty(),
                Set.of("USER")
        );
        when(actorContextMapper.map(any(Authentication.class))).thenReturn(actorContext);
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private AiConversacion buildConversacion(UUID id) {
        return AiConversacion.reconstitute(
                AiConversacionId.from(id),
                EmpresaId.from(EMPRESA_ID),
                UsuarioId.from(ACTOR_USUARIO_ID),
                "wa-conv-1",
                null,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private AiMensaje buildMensaje(UUID id, UUID aiConvId, RolMensajeAi rol, String contenido) {
        return AiMensaje.reconstitute(
                AiMensajeId.from(id),
                AiConversacionId.from(aiConvId),
                rol,
                contenido,
                "claude-haiku-4-5",
                100,
                50,
                250L,
                null,
                LocalDateTime.now()
        );
    }

    private AiAccion buildAccion(UUID id, EstadoAccion estado) {
        return AiAccion.reconstitute(
                AiAccionId.from(id),
                EmpresaId.from(EMPRESA_ID),
                UsuarioId.from(ACTOR_USUARIO_ID),
                "wa-conv-1",
                null,
                AiConversacionId.from(UUID.randomUUID()),
                "CREATE_CONTACTO",
                "{\"nombre\":\"stub\"}",
                "r",
                1,
                LocalDateTime.now().plusHours(1),
                null,
                null,
                estado,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ── POST /api/ai/chat ──────────────────────────────────────────

    @Test
    void chat_shouldReturn200WithAssistantReply() throws Exception {
        UUID aiConvId = UUID.randomUUID();
        when(analizarChatUseCase.analizar(any())).thenReturn(
                new ResultadoAnalisisChat(aiConvId, "Hola, ¿en qué te ayudo?", "claude-haiku-4-5")
        );

        String body = """
                {
                  "waConversacionId": "wa-conv-1",
                  "mensajeUsuario": "Hola"
                }
                """;

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiConversacionId").value(aiConvId.toString()))
                .andExpect(jsonPath("$.contenidoAsistente").value("Hola, ¿en qué te ayudo?"))
                .andExpect(jsonPath("$.modelo").value("claude-haiku-4-5"));

        // PR4/PR5 corrective: the controller never forwards a tenant hint
        // for /chat. The use case derives the tenant from the WhatsApp
        // conversation resource, so the command MUST carry empresaId=null
        // here. /chat also does NOT accept an empresaId request param.
        verify(analizarChatUseCase).analizar(argThat(cmd ->
                ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && cmd.empresaId() == null
                        && "wa-conv-1".equals(cmd.waConversacionId())
                        && "Hola".equals(cmd.mensajeUsuario())
        ));
    }

    @Test
    void chat_multiCompanyActor_shouldNeverForwardEmpresaIdHint() throws Exception {
        // PR4/PR5 corrective: the controller never forwards a tenant hint
        // for /chat and never reads one from the actor. The use case
        // derives the tenant from the WhatsApp conversation resource.
        // This IT pins the controller boundary contract: /chat never
        // carries empresaId to the use case.
        UUID aiConvId = UUID.randomUUID();
        when(analizarChatUseCase.analizar(any())).thenReturn(
                new ResultadoAnalisisChat(aiConvId, "Respuesta", "claude-haiku-4-5")
        );

        String body = """
                {
                  "waConversacionId": "wa-conv-2",
                  "mensajeUsuario": "Hola multi-empresa"
                }
                """;

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiConversacionId").value(aiConvId.toString()));

        // The command MUST carry empresaId=null regardless of any actor
        // identity. The use case is responsible for deriving tenant
        // authority from the resource.
        verify(analizarChatUseCase).analizar(argThat(cmd ->
                ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && cmd.empresaId() == null
                        && "wa-conv-2".equals(cmd.waConversacionId())
                        && "Hola multi-empresa".equals(cmd.mensajeUsuario())
        ));
    }

    @Test
    void chat_shouldReturn500Propagated_whenUseCaseThrowsResourceTenantException() throws Exception {
        // PR5 defensive: when the WhatsApp conversation resource belongs to a
        // company the actor does not own, the use case throws
        // AsistenteTenantException (mapped to 403 by GlobalExceptionHandler).
        // The controller must surface this without ever leaking a /chat
        // command that would let the hint win.
        when(analizarChatUseCase.analizar(any()))
                .thenThrow(com.ar.crm2.application.ai.exception.AsistenteTenantException
                        .chatNoPerteneceAlActor(ACTOR_USUARIO_ID.toString(), "wa-conv-3"));

        String body = """
                {
                  "waConversacionId": "wa-conv-3",
                  "mensajeUsuario": "no me pertenece"
                }
                """;

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        verify(analizarChatUseCase).analizar(argThat(cmd ->
                cmd.empresaId() == null && "wa-conv-3".equals(cmd.waConversacionId())
        ));
    }

    @Test
    void chat_shouldReturn502_whenUseCaseThrowsAiAssistantException() throws Exception {
        // Audit #3 closing IT: when the AI chat layer (or any application
        // service below /chat) throws AiAssistantException — upstream
        // failure, malformed model output, etc. — the REST boundary MUST
        // surface HTTP 502 Bad Gateway with the documented error body.
        // Previously, GlobalExceptionHandler had no @ExceptionHandler for
        // AiAssistantException and the boundary would have leaked as a
        // generic 500. The exception's own Javadoc documents "Maps to
        // HTTP 502 Bad Gateway at the REST boundary" — that contract is
        // now enforceable end-to-end.
        when(analizarChatUseCase.analizar(any()))
                .thenThrow(com.ar.crm2.application.ai.exception.AiAssistantException
                        .upstreamFailure("ChatClient call failed: provider 504"));

        String body = """
                {
                  "waConversacionId": "wa-conv-502",
                  "mensajeUsuario": "hola"
                }
                """;

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error",
                        org.hamcrest.Matchers.containsString("provider 504")));
    }

    // ── POST /api/ai/acciones/{id}/confirmar ──────────────────────

    @Test
    void confirmarAccion_shouldReturn200WithExecutedResult() throws Exception {
        UUID accionId = UUID.randomUUID();
        when(confirmarAccionUseCase.confirmar(any())).thenReturn(
                ResultadoEjecucionAccion.ejecutada("new-contact-id", 2)
        );

        mockMvc.perform(post("/api/ai/acciones/{id}/confirmar", accionId)
                        .param("expectedVersion", "1")
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXECUTED"))
                .andExpect(jsonPath("$.resultadoEntidadId").value("new-contact-id"))
                .andExpect(jsonPath("$.nuevaVersion").value(2));

        verify(confirmarAccionUseCase).confirmar(argThat(cmd ->
                accionId.equals(cmd.accionId())
                        && ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && EMPRESA_ID.equals(cmd.empresaId())
                        && cmd.expectedVersion() == 1
        ));
    }

    @Test
    void confirmarAccion_shouldReturn403WhenTenantMismatch() throws Exception {
        UUID accionId = UUID.randomUUID();
        doThrow(ConversacionAsistenteNotOwnedByActorException.tenantMismatch(
                        ACTOR_USUARIO_ID.toString(), accionId.toString()))
                .when(confirmarAccionUseCase).confirmar(any());

        mockMvc.perform(post("/api/ai/acciones/{id}/confirmar", accionId)
                        .param("expectedVersion", "1")
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void confirmarAccion_shouldReturn409OnReplay() throws Exception {
        UUID accionId = UUID.randomUUID();
        doThrow(AccionStateException.invalidState(accionId.toString(), "confirmar", "CONFIRMED"))
                .when(confirmarAccionUseCase).confirmar(any());

        mockMvc.perform(post("/api/ai/acciones/{id}/confirmar", accionId)
                        .param("expectedVersion", "1")
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void confirmarAccion_shouldReturn409OnVersionMismatch() throws Exception {
        UUID accionId = UUID.randomUUID();
        doThrow(AccionVersionMismatchException.mismatch(accionId.toString(), 1, 3))
                .when(confirmarAccionUseCase).confirmar(any());

        mockMvc.perform(post("/api/ai/acciones/{id}/confirmar", accionId)
                        .param("expectedVersion", "1")
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void confirmarAccion_shouldReturn404WhenAccionMissing() throws Exception {
        UUID accionId = UUID.randomUUID();
        doThrow(AccionNotFoundException.forId(accionId))
                .when(confirmarAccionUseCase).confirmar(any());

        mockMvc.perform(post("/api/ai/acciones/{id}/confirmar", accionId)
                        .param("expectedVersion", "1")
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void confirmarAccion_empresaSeleccionadaVsRecurso_returns403WithControlledMessage() throws Exception {
        // PR6 cross-check at the controller boundary: when the request's
        // empresaId differs from the addressed AiAccion resource's
        // empresaId, the use case throws a controlled
        // AsistenteTenantException("accion ... no pertenece a la empresa
        // seleccionada ..."). The GlobalExceptionHandler maps this to
        // HTTP 403 — never a generic AI/runtime error. The controller's
        // job here is to (a) forward the empresaId verbatim via the
        // AiRestCommandMapper and (b) propagate the exception as 403.
        UUID accionId = UUID.randomUUID();
        UUID empresaSeleccionada = UUID.fromString("99999999-9999-9999-9999-999999999999"); // != EMPRESA_ID
        doThrow(
                com.ar.crm2.application.ai.exception.AsistenteTenantException
                        .accionNoPerteneceALaEmpresaSeleccionada(
                                accionId.toString(), empresaSeleccionada.toString()
                        )
        ).when(confirmarAccionUseCase).confirmar(any());

        mockMvc.perform(post("/api/ai/acciones/{id}/confirmar", accionId)
                        .param("expectedVersion", "1")
                        .param("empresaId", empresaSeleccionada.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.containsString("no pertenece a la empresa seleccionada")));

        // The mapper MUST forward the request's empresaId verbatim — the
        // service is the one that rejects the mismatch.
        verify(confirmarAccionUseCase).confirmar(argThat(cmd ->
                accionId.equals(cmd.accionId())
                        && ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && empresaSeleccionada.equals(cmd.empresaId())
                        && cmd.expectedVersion() == 1
        ));
    }

    // ── POST /api/ai/acciones/{id}/rechazar ───────────────────────

    @Test
    void rechazarAccion_shouldReturn200WithRejectedAccion() throws Exception {
        UUID accionId = UUID.randomUUID();
        AiAccion rechazada = buildAccion(accionId, EstadoAccion.REJECTED);
        when(rechazarAccionUseCase.rechazar(any())).thenReturn(rechazada);

        mockMvc.perform(post("/api/ai/acciones/{id}/rechazar", accionId)
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accionId.toString()))
                .andExpect(jsonPath("$.estado").value("REJECTED"));

        verify(rechazarAccionUseCase).rechazar(argThat(cmd ->
                accionId.equals(cmd.accionId())
                        && ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && EMPRESA_ID.equals(cmd.empresaId())
        ));
    }

    @Test
    void rechazarAccion_empresaSeleccionadaVsRecurso_returns403WithControlledMessage() throws Exception {
        // PR6 cross-check at the controller boundary (rejection path):
        // when the request's empresaId differs from the addressed
        // AiAccion resource's stored empresaId, the use case throws
        // the controlled cross-check exception. The GlobalExceptionHandler
        // maps it to HTTP 403 with the
        // "no pertenece a la empresa seleccionada" message — NEVER a
        // generic AI runtime error.
        UUID accionId = UUID.randomUUID();
        UUID empresaSeleccionada = UUID.fromString("99999999-9999-9999-9999-999999999999"); // != EMPRESA_ID
        doThrow(
                com.ar.crm2.application.ai.exception.AsistenteTenantException
                        .accionNoPerteneceALaEmpresaSeleccionada(
                                accionId.toString(), empresaSeleccionada.toString()
                        )
        ).when(rechazarAccionUseCase).rechazar(any());

        mockMvc.perform(post("/api/ai/acciones/{id}/rechazar", accionId)
                        .param("empresaId", empresaSeleccionada.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.containsString("no pertenece a la empresa seleccionada")));

        verify(rechazarAccionUseCase).rechazar(argThat(cmd ->
                accionId.equals(cmd.accionId())
                        && ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && empresaSeleccionada.equals(cmd.empresaId())
        ));
    }

    // ── GET /api/ai/conversaciones/{id} ───────────────────────────

    @Test
    void obtenerConversacion_shouldReturn200WithConversationAndMessages() throws Exception {
        UUID aiConvId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();
        AiConversacion conv = buildConversacion(aiConvId);
        AiMensaje msg = buildMensaje(msgId, aiConvId, RolMensajeAi.USER, "Hola");

        when(obtenerConversacionAsistenteUseCase.obtener(any())).thenReturn(
                new ObtenerConversacionAsistenteUseCase.ResultadoConversacionAsistente(conv, List.of(msg))
        );

        mockMvc.perform(get("/api/ai/conversaciones/{id}", aiConvId)
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversacion.id").value(aiConvId.toString()))
                .andExpect(jsonPath("$.mensajes[0].id").value(msgId.toString()))
                .andExpect(jsonPath("$.mensajes[0].rol").value("USER"))
                .andExpect(jsonPath("$.mensajes[0].contenido").value("Hola"));
    }

    @Test
    void obtenerConversacion_shouldReturn404WhenMissing() throws Exception {
        UUID aiConvId = UUID.randomUUID();
        when(obtenerConversacionAsistenteUseCase.obtener(any()))
                .thenThrow(ConversacionAsistenteNoEncontradaException.forId(aiConvId.toString()));

        mockMvc.perform(get("/api/ai/conversaciones/{id}", aiConvId)
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void obtenerConversacion_empresaSeleccionadaVsRecurso_returns403WithControlledMessage() throws Exception {
        // PR6 cross-check at the controller boundary (conversation read):
        // mirrors confirmar/rechazar but for the GET endpoint — when the
        // request's empresaId differs from the addressed AiConversacion
        // resource's stored empresaId, the use case throws the controlled
        // AsistenteTenantException.conversacionNoPerteneceALaEmpresaSeleccionada(...).
        // The GlobalExceptionHandler maps it to HTTP 403 with the
        // "no pertenece a la empresa seleccionada" message.
        UUID aiConvId = UUID.randomUUID();
        UUID empresaSeleccionada = UUID.fromString("99999999-9999-9999-9999-999999999999"); // != EMPRESA_ID
        when(obtenerConversacionAsistenteUseCase.obtener(any()))
                .thenThrow(
                        com.ar.crm2.application.ai.exception.AsistenteTenantException
                                .conversacionNoPerteneceALaEmpresaSeleccionada(
                                        aiConvId.toString(), empresaSeleccionada.toString()
                                )
                );

        mockMvc.perform(get("/api/ai/conversaciones/{id}", aiConvId)
                        .param("empresaId", empresaSeleccionada.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.containsString("no pertenece a la empresa seleccionada")));
    }

    // ── GET /api/ai/acciones ───────────────────────────────────────

    @Test
    void listarAcciones_shouldReturn200WithPendingAccionList() throws Exception {
        UUID accionId = UUID.randomUUID();
        AiAccion pending = buildAccion(accionId, EstadoAccion.PENDING);
        when(listarAccionesPendientesUseCase.listar(any())).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/ai/acciones")
                        .param("empresaId", EMPRESA_ID.toString())
                        .param("limite", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accionId.toString()))
                .andExpect(jsonPath("$[0].estado").value("PENDING"))
                .andExpect(jsonPath("$[0].tipoAccion").value("CREATE_CONTACTO"));

        verify(listarAccionesPendientesUseCase).listar(argThat(cmd ->
                ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && EMPRESA_ID.equals(cmd.empresaId())
                        && cmd.limite() == 50
        ));
    }

    @Test
    void listarAcciones_empresaIdFaltante_returns400WithControlledMessage() throws Exception {
        // PR7 contract: missing empresaId MUST be rejected with HTTP 400,
        // never silently produce an empty list and never auto-resolve a
        // single owned company. Validation belongs in the request DTO.
        mockMvc.perform(get("/api/ai/acciones")
                        .param("limite", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error",
                        org.hamcrest.Matchers.containsString("empresaId")));

        org.mockito.Mockito.verifyNoInteractions(listarAccionesPendientesUseCase);
    }

    @Test
    void listarAcciones_limiteFueraDeRango_returns400WithControlledMessage() throws Exception {
        // PR7 contract: out-of-range limite MUST be rejected with HTTP 400
        // at the REST boundary. The application command's canonical
        // constructor enforces the same invariant (1..200) as defense
        // in depth.
        mockMvc.perform(get("/api/ai/acciones")
                        .param("empresaId", EMPRESA_ID.toString())
                        .param("limite", "9999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error",
                        org.hamcrest.Matchers.containsString("limite")));

        org.mockito.Mockito.verifyNoInteractions(listarAccionesPendientesUseCase);
    }

    @Test
    void listarAcciones_actorNoPoseeEmpresa_returns403WithControlledMessage() throws Exception {
        // PR7 contract: when the supplied empresaId is not owned by the
        // actor, the application service raises AsistenteTenantException
        // (mapped to 403). The mapper MUST forward the request's
        // empresaId verbatim so the service is the one rejecting.
        UUID empresaSeleccionada = UUID.fromString("99999999-9999-9999-9999-999999999999"); // != EMPRESA_ID
        doThrow(
                com.ar.crm2.application.ai.exception.AsistenteTenantException
                        .tenantSelectorRechazado(
                                ACTOR_USUARIO_ID.toString(), empresaSeleccionada.toString()
                        )
        ).when(listarAccionesPendientesUseCase).listar(any());

        mockMvc.perform(get("/api/ai/acciones")
                        .param("empresaId", empresaSeleccionada.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.containsString("selector")));

        verify(listarAccionesPendientesUseCase).listar(argThat(cmd ->
                ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && empresaSeleccionada.equals(cmd.empresaId())
        ));
    }

    @Test
    void listarAcciones_limiteNullUsaDefault50() throws Exception {
        // When the caller omits the limite query parameter, the mapper
        // applies the default of 50 (mirrors the previous controller
        // default). The use case still receives the explicit value.
        AiAccion pending = buildAccion(UUID.randomUUID(), EstadoAccion.PENDING);
        when(listarAccionesPendientesUseCase.listar(any())).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/ai/acciones")
                        .param("empresaId", EMPRESA_ID.toString()))
                .andExpect(status().isOk());

        verify(listarAccionesPendientesUseCase).listar(argThat(cmd ->
                ACTOR_USUARIO_ID.equals(cmd.actorUsuarioId())
                        && EMPRESA_ID.equals(cmd.empresaId())
                        && cmd.limite() == 50
        ));
    }
}