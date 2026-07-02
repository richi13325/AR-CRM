package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesResponse;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.UUID;

/**
 * {@code @Tool}-annotated input adapter that returns the most recent
 * WhatsApp messages of the conversation the AI assistant is currently
 * bound to, capped by a model-supplied {@code limit}.
 *
 * <p><b>Trust boundary.</b> the {@code waConversacionId} is sourced
 * from the trusted {@link AiToolContextPort} and is NEVER on the
 * wire — the request DTO carries only {@code limit}. The model
 * cannot probe a foreign conversation because there is no field to
 * supply. Tenant authorization is enforced upstream by the AI
 * application service that bound the context around
 * {@code ChatClient.call()}.
 *
 * <p><b>Read-only by design.</b> the tool depends ONLY on the
 * trusted scope port ({@link AiToolContextPort}) and the
 * {@link WhatsappMensajeLecturaPort} read port, never on a real
 * CRM mutation use case. The constructor guard in
 * {@code ObtenerMensajesRecientesToolTest#constructor_doesNotInjectRealMutationUseCases}
 * pins this contract.
 *
 * <p><b>Mapping.</b> the tool delegates all DTO ↔ projection
 * translation to {@link ObtenerMensajesRecientesToolMapper}, mirroring
 * the {@code ProponerAccionTool} ↔ {@code ProponerAccionToolMapper}
 * pattern. The tool class itself never assembles wire shapes
 * inline.
 *
 * <p><b>Failure mode.</b> if no context is bound (the tool was
 * invoked outside the AI request flow), the upstream
 * {@link AiToolContextPort#resolve()} throws
 * {@link IllegalStateException}; the tool wraps it into the
 * application-owned {@link AiAssistantException} so the boundary
 * stays framework-free and the REST layer can map it to a 502.
 * The read port is NEVER invoked in that case — there is no
 * fall-back conversation id.
 */
@Slf4j
@RequiredArgsConstructor
public class ObtenerMensajesRecientesTool {

    private final AiToolContextPort aiToolContextPort;
    private final WhatsappMensajeLecturaPort whatsappMensajeLecturaPort;

    /**
     * Loads the most recent N messages of the trusted WhatsApp
     * conversation (resolved from {@link AiToolContextPort}). The
     * model invokes this tool whenever it needs to inspect the chat
     * transcript (e.g. before deciding whether to propose an action).
     *
     * @param request model-supplied {@code limit} cap (1..50)
     * @return chronologically-ascending list of up to {@code limit}
     *         messages; never null
     * @throws AiAssistantException when the trusted tool context
     *         cannot be resolved (caller invoked the tool outside
     *         the AI request flow)
     */
    @Tool(
        name = "obtenerMensajesRecientes",
        description = "Returns the most recent N WhatsApp messages of the conversation the "
            + "assistant is currently bound to. Use this to inspect the chat transcript "
            + "before proposing a CRM action. Read-only — does NOT create, update, or move "
            + "any CRM entity. The conversation is resolved from the trusted AI context, "
            + "not from the model payload."
    )
    public List<ObtenerMensajesRecientesResponse> obtenerMensajesRecientes(
            ObtenerMensajesRecientesRequest request) {
        AiToolContext ctx;
        try {
            ctx = aiToolContextPort.resolve();
        } catch (IllegalStateException ex) {
            throw AiAssistantException.upstreamFailure(
                "AI tool invoked without trusted AiToolContext: " + ex.getMessage(), ex);
        }
        UUID waConvId = UUID.fromString(ctx.waConversacionId());
        int limit = ObtenerMensajesRecientesToolMapper.limitOf(request);

        List<WhatsappMensajeResumen> messages =
            whatsappMensajeLecturaPort.findByConversacionId(waConvId);

        List<WhatsappMensajeResumen> capped =
            ObtenerMensajesRecientesToolMapper.takeLastN(messages, limit);

        List<ObtenerMensajesRecientesResponse> out =
            ObtenerMensajesRecientesToolMapper.toResponseList(capped);

        log.info(
            "AI tool read {} messages (capped to {}, total {} returned) for trusted "
                + "waConversacionId={} (aiConversacionId={}, actorUsuarioId={}, empresaId={})",
            messages.size(), limit, out.size(), waConvId,
            ctx.aiConversacionId(), ctx.actorUsuarioId(), ctx.empresaId()
        );
        return out;
    }
}