package com.ar.crm2.adapter.in.rest.ai;

import com.ar.crm2.adapter.in.rest.dto.ai.AccionEjecutadaResponse;
import com.ar.crm2.adapter.in.rest.dto.ai.AccionPendienteResponse;
import com.ar.crm2.adapter.in.rest.dto.ai.AccionRechazadaResponse;
import com.ar.crm2.adapter.in.rest.dto.ai.AiMensajeResponse;
import com.ar.crm2.adapter.in.rest.dto.ai.AnalizarChatRequest;
import com.ar.crm2.adapter.in.rest.dto.ai.ConversacionAsistenteResponse;
import com.ar.crm2.adapter.in.rest.dto.ai.ListarAccionesPendientesRequest;
import com.ar.crm2.application.ai.command.AnalizarChatCommand;
import com.ar.crm2.application.ai.command.ConfirmarAccionCommand;
import com.ar.crm2.application.ai.command.ListarAccionesPendientesCommand;
import com.ar.crm2.application.ai.command.ObtenerConversacionAsistenteCommand;
import com.ar.crm2.application.ai.command.RechazarAccionCommand;
import com.ar.crm2.application.ai.port.in.AnalizarChatUseCase;
import com.ar.crm2.application.ai.port.in.ConfirmarAccionUseCase;
import com.ar.crm2.application.ai.port.in.ListarAccionesPendientesUseCase;
import com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase;
import com.ar.crm2.application.ai.port.in.RechazarAccionUseCase;
import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;
import com.ar.crm2.application.ai.port.in.result.ResultadoEjecucionAccion;
import com.ar.crm2.application.security.ActorContext;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.security.ActorContextRequestAttributeFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the AI assistant (PR 4 slice).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/ai/chat} — analyze a WhatsApp conversation
 *       and obtain the assistant's first-turn reply.</li>
 *   <li>{@code POST /api/ai/acciones/{id}/confirmar} — confirm a
 *       PENDING AI-proposed action (two-step user-driven flow).</li>
 *   <li>{@code POST /api/ai/acciones/{id}/rechazar} — reject a
 *       PENDING AI-proposed action.</li>
 *   <li>{@code GET /api/ai/conversaciones/{id}} — fetch one AI
 *       conversation and its ordered message history.</li>
 *   <li>{@code GET /api/ai/acciones} — list the requester's
 *       {@code PENDING} AI action proposals.</li>
 * </ul>
 *
 * <p><b>Trust boundary:</b> the {@link ActorContext} is sourced from
 * the request attribute populated by
 * {@link ActorContextRequestAttributeFilter}; never from the body or
 * query string. The use cases handle tenant scoping and ownership
 * enforcement; the controller only translates HTTP ↔ commands and
 * delegates to inbound use cases.
 *
 * <p><b>Command construction:</b> every inbound command is built by
 * {@link AiRestCommandMapper}, not instantiated inline in this class.
 * The controller stays focused on HTTP wiring (status codes, headers,
 * query/path binding) and response assembly ({@code fromDomain(...)}
 * static calls on the REST DTOs).
 *
 * <p><b>Tenant authority (PR4/PR5 corrective):</b>
 * {@link ActorContext} is a pure identity record and does NOT carry a
 * tenant hint. Endpoints that need an explicit tenant receive it as a
 * required {@code @RequestParam("empresaId") UUID empresaId}; the
 * {@code /chat} endpoint is resource-first and never accepts an
 * {@code empresaId} parameter — the application service derives
 * tenant authority from the addressed WhatsApp conversation.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AnalizarChatUseCase analizarChatUseCase;
    private final ConfirmarAccionUseCase confirmarAccionUseCase;
    private final RechazarAccionUseCase rechazarAccionUseCase;
    private final ObtenerConversacionAsistenteUseCase obtenerConversacionAsistenteUseCase;
    private final ListarAccionesPendientesUseCase listarAccionesPendientesUseCase;

    // ── POST /api/ai/chat ──────────────────────────────────────────

    /**
     * Runs the AI assistant on the supplied WhatsApp conversation.
     *
     * <p>The {@code mensajeUsuario} field is optional: when null/blank,
     * the request represents a "first turn" without a kick-off
     * question.
     *
     * <p><b>Resource-first tenant resolution (PR5):</b> the controller
     * does NOT accept an {@code empresaId} parameter for this endpoint
     * and never forwards a tenant hint from the actor. The application
     * service derives tenant authority from the addressed WhatsApp
     * conversation's {@code canalEmpresaId} and ignores any
     * client-supplied hint.
     */
    @PostMapping("/chat")
    public ResponseEntity<AiMensajeResponse> chat(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AnalizarChatRequest request
    ) {
        AnalizarChatCommand command = AiRestCommandMapper.toCommand(
                actorContext(httpRequest), request
        );
        ResultadoAnalisisChat result = analizarChatUseCase.analizar(command);
        return ResponseEntity.ok(AiMensajeResponse.fromDomain(result));
    }

    // ── POST /api/ai/acciones/{id}/confirmar ──────────────────────

    /**
     * Confirms a PENDING AI-proposed action. The same actor that
     * staged the proposal must be the one that confirms it — the
     * use case enforces owner + tenant + state + version + expiry.
     *
     * <p>{@code empresaId} is supplied as a required query parameter
     * because {@link ActorContext} no longer carries a tenant hint.
     */
    @PostMapping("/acciones/{id}/confirmar")
    public ResponseEntity<AccionEjecutadaResponse> confirmarAccion(
            HttpServletRequest httpRequest,
            @PathVariable UUID id,
            @RequestParam("expectedVersion") int expectedVersion,
            @RequestParam("empresaId") UUID empresaId
    ) {
        ConfirmarAccionCommand command = AiRestCommandMapper.toCommand(
                actorContext(httpRequest), id, expectedVersion, empresaId
        );
        ResultadoEjecucionAccion result = confirmarAccionUseCase.confirmar(command);
        return ResponseEntity.ok(AccionEjecutadaResponse.fromDomain(result));
    }

    // ── POST /api/ai/acciones/{id}/rechazar ───────────────────────

    /**
     * Rejects a PENDING AI-proposed action. Side-effect free at the
     * CRM layer — only the proposal state transitions to REJECTED.
     *
     * <p>{@code empresaId} is supplied as a required query parameter
     * because {@link ActorContext} no longer carries a tenant hint.
     */
    @PostMapping("/acciones/{id}/rechazar")
    public ResponseEntity<AccionRechazadaResponse> rechazarAccion(
            HttpServletRequest httpRequest,
            @PathVariable UUID id,
            @RequestParam("empresaId") UUID empresaId
    ) {
        RechazarAccionCommand command = AiRestCommandMapper.toRechazarAccionCommand(
                actorContext(httpRequest), id, empresaId
        );
        AiAccion result = rechazarAccionUseCase.rechazar(command);
        return ResponseEntity.ok(AccionRechazadaResponse.fromDomain(result));
    }

    // ── GET /api/ai/conversaciones/{id} ───────────────────────────

    /**
     * Returns one AI conversation (always non-null when found) and
     * its ordered message history. Owner + tenant enforcement lives
     * on the {@code AiConversacion.requireOwnedBy(...)} aggregate
     * guard — a cross-tenant request yields 403 via the Global
     * Exception Handler.
     *
     * <p>{@code empresaId} is supplied as a required query parameter
     * because {@link ActorContext} no longer carries a tenant hint.
     */
    @GetMapping("/conversaciones/{id}")
    public ResponseEntity<ConversacionAsistenteResponse> obtenerConversacion(
            HttpServletRequest httpRequest,
            @PathVariable UUID id,
            @RequestParam("empresaId") UUID empresaId
    ) {
        ObtenerConversacionAsistenteCommand command = AiRestCommandMapper.toObtenerConversacionCommand(
                actorContext(httpRequest), id, empresaId
        );
        ObtenerConversacionAsistenteUseCase.ResultadoConversacionAsistente result =
                obtenerConversacionAsistenteUseCase.obtener(command);
        return ResponseEntity.ok(ConversacionAsistenteResponse.fromDomain(result));
    }

    // ── GET /api/ai/acciones ───────────────────────────────────────

    /**
     * Lists the requester's PENDING AI action proposals scoped to the
     * supplied tenant. Surfaces the dedicated
     * {@code ListarAccionesPendientesUseCase}.
     *
     * <p><b>User-approved PR7 contract:</b> the {@code empresaId}
     * selector is REQUIRED. Validation belongs in
     * {@link ListarAccionesPendientesRequest}; the controller
     * forwards the bound DTO to {@link AiRestCommandMapper}, which
     * produces the {@code ListarAccionesPendientesCommand}. A missing
     * {@code empresaId} triggers the
     * {@code GlobalExceptionHandler} validation path that maps the
     * constraint violation to HTTP 400 — NEVER a silently empty list
     * and NEVER an auto-resolved tenant.
     */
    @GetMapping("/acciones")
    public ResponseEntity<List<AccionPendienteResponse>> listarAcciones(
            HttpServletRequest httpRequest,
            @Valid ListarAccionesPendientesRequest request
    ) {
        ListarAccionesPendientesCommand command = AiRestCommandMapper.toListarAccionesPendientesCommand(
                actorContext(httpRequest), request
        );
        List<AiAccion> pendientes = listarAccionesPendientesUseCase.listar(command);
        List<AccionPendienteResponse> body = pendientes.stream()
                .map(AccionPendienteResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(body);
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private ActorContext actorContext(HttpServletRequest httpRequest) {
        return (ActorContext) httpRequest.getAttribute(
                ActorContextRequestAttributeFilter.ACTOR_CONTEXT_ATTRIBUTE
        );
    }
}
