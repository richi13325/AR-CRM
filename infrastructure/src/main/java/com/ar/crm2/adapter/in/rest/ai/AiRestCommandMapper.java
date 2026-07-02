package com.ar.crm2.adapter.in.rest.ai;

import com.ar.crm2.adapter.in.rest.dto.ai.AnalizarChatRequest;
import com.ar.crm2.adapter.in.rest.dto.ai.ListarAccionesPendientesRequest;
import com.ar.crm2.application.ai.command.AnalizarChatCommand;
import com.ar.crm2.application.ai.command.ConfirmarAccionCommand;
import com.ar.crm2.application.ai.command.ListarAccionesPendientesCommand;
import com.ar.crm2.application.ai.command.ObtenerConversacionAsistenteCommand;
import com.ar.crm2.application.ai.command.RechazarAccionCommand;
import com.ar.crm2.application.security.ActorContext;

import java.util.UUID;

/**
 * REST-side mapper that builds application commands from the AI REST
 * controller's inbound payload (path variables, query parameters, JSON
 * body, and the trusted {@link ActorContext}).
 *
 * <p>The mapper is the single boundary that translates HTTP-layer
 * concerns (path variables, query parameters, JSON DTOs) into the
 * application command records. The {@link AiController} does NOT
 * instantiate commands directly — every command is built through one
 * of the command-specific methods on this mapper. This keeps
 * the controller focused on HTTP wiring (status codes, headers,
 * query/path binding) and the validation/trust-boundary contract.
 *
 * <p>Trust boundary: the {@link ActorContext} is the trusted source
 * for the requester identity. The mapper reads
 * {@code actor.usuarioId()} and never falls back to a body field —
 * that would let a model payload override the authenticated actor.
 * Tenant selection follows the resource-first + explicit-hint rules:
 * <ul>
 *   <li>{@code /chat}: the command always carries
 *       {@code empresaId = null} (resource-first; the WhatsApp
 *       conversation supplies the tenant authority downstream).</li>
 *   <li>Resource-bound endpoints (confirm / reject / get
 *       conversation): the controller forwards the
 *       {@code empresaId} query parameter verbatim; the application
 *       layer verifies ownership before trusting it.</li>
 *   <li>Selector endpoint (list pending actions,
 *       {@code /api/ai/acciones}): the controller binds a
 *       {@link ListarAccionesPendientesRequest} DTO whose
 *       {@code @NotNull empresaId} constraint enforces the user-
 *       approved PR7 contract that the selector is REQUIRED.</li>
 * </ul>
 */
public final class AiRestCommandMapper {

    /**
     * Default {@code limite} applied when the
     * {@link ListarAccionesPendientesRequest#limite()} is null
     * (i.e. the caller omitted the query parameter). Mirrors the
     * previous controller default of {@code 50}.
     */
    private static final int DEFAULT_LISTAR_ACCIONES_LIMITE = 50;

    private AiRestCommandMapper() {
    }

    /**
     * Builds the {@link AnalizarChatCommand} for {@code POST /api/ai/chat}.
     *
     * <p>The {@code empresaId} is intentionally {@code null}: the
     * resource-first tenant model derives tenant authority from the
     * addressed WhatsApp conversation ({@code canalEmpresaId}) inside
     * the application service. Forwarding any tenant hint at this
     * endpoint would defeat the resource-first contract.
     */
    public static AnalizarChatCommand toCommand(
            ActorContext actor,
            AnalizarChatRequest request
    ) {
        return new AnalizarChatCommand(
                actor.usuarioId().orElse(null),
                null,
                request.waConversacionId(),
                request.mensajeUsuario()
        );
    }

    /**
     * Builds the {@link ConfirmarAccionCommand} for
     * {@code POST /api/ai/acciones/{id}/confirmar}.
     */
    public static ConfirmarAccionCommand toCommand(
            ActorContext actor,
            UUID accionId,
            int expectedVersion,
            UUID empresaId
    ) {
        return new ConfirmarAccionCommand(
                actor.usuarioId().orElse(null),
                accionId,
                expectedVersion,
                empresaId
        );
    }

    /**
     * Builds the {@link RechazarAccionCommand} for
     * {@code POST /api/ai/acciones/{id}/rechazar}.
     */
    public static RechazarAccionCommand toRechazarAccionCommand(
            ActorContext actor,
            UUID accionId,
            UUID empresaId
    ) {
        return new RechazarAccionCommand(
                actor.usuarioId().orElse(null),
                accionId,
                empresaId
        );
    }

    /**
     * Builds the {@link ObtenerConversacionAsistenteCommand} for
     * {@code GET /api/ai/conversaciones/{id}}.
     */
    public static ObtenerConversacionAsistenteCommand toObtenerConversacionCommand(
            ActorContext actor,
            UUID aiConversacionId,
            UUID empresaId
    ) {
        return new ObtenerConversacionAsistenteCommand(
                actor.usuarioId().orElse(null),
                aiConversacionId,
                empresaId
        );
    }

    /**
     * Builds the {@link ListarAccionesPendientesCommand} for
     * {@code GET /api/ai/acciones} from the validated
     * {@link ListarAccionesPendientesRequest} DTO +
     * {@link ActorContext}.
     *
     * <p><b>User-approved PR7 contract:</b> the DTO's {@code @NotNull}
     * constraint on {@code empresaId} is the REST boundary enforcement
     * for the mandatory selector. A {@code null} {@code empresaId}
     * therefore MUST NOT reach this mapper in normal flow; the command
     * canonical constructor enforces the same invariant as defense in
     * depth, so any alternate bypass path still fails closed.
     *
     * <p>The {@code limite} is an {@link Integer} so {@code null} means
     * "use default {@value #DEFAULT_LISTAR_ACCIONES_LIMITE}". The
     * controller binds the DTO via Spring's query-param binding and
     * forwards the result here; the mapper supplies the default for
     * a missing parameter so the application command stays simple.
     *
     * @param actor   trusted authenticated actor (identity anchor)
     * @param request the validated request DTO (selector + page size)
     * @return the application command
     */
    public static ListarAccionesPendientesCommand toListarAccionesPendientesCommand(
            ActorContext actor,
            ListarAccionesPendientesRequest request
    ) {
        int limite = request.limite() != null
                ? request.limite()
                : DEFAULT_LISTAR_ACCIONES_LIMITE;
        return new ListarAccionesPendientesCommand(
                actor.usuarioId().orElse(null),
                request.empresaId(),
                limite
        );
    }
}
