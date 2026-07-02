package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerResumenChatResponse;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Optional;

/**
 * {@code @Tool}-annotated input adapter that returns the persisted
 * {@code AiResumenContexto} for the current AI conversation.
 *
 * <p><b>Parameterless.</b> the {@code aiConversacionId} is resolved
 * from the trusted {@link AiToolContextPort} — the model cannot
 * spoof a foreign conversation because the tool accepts no
 * parameters. Tenant authorization is enforced upstream by the AI
 * application service that bound the context.
 *
 * <p><b>Failure mode.</b> if no context is bound (the tool was
 * invoked outside the AI request flow), the upstream
 * {@link AiToolContextPort#resolve()} throws
 * {@link IllegalStateException}; the tool wraps it into the
 * application-owned {@link AiAssistantException} so the boundary
 * stays framework-free and the REST layer can map it to a 502.
 *
 * <p><b>Read-only by design.</b> the tool depends ONLY on the
 * {@link FindAiResumenPort} read port and never on a real CRM
 * mutation use case. The constructor guard in
 * {@code ObtenerResumenChatToolTest#constructor_doesNotInjectRealMutationUseCases}
 * pins this contract.
 */
@Slf4j
@RequiredArgsConstructor
public class ObtenerResumenChatTool {

    private final AiToolContextPort aiToolContextPort;
    private final FindAiResumenPort findAiResumenPort;

    /**
     * Returns the persisted context summary for the current AI
     * conversation, or a placeholder record when none exists yet.
     *
     * @return the summary as a wire DTO; never null
     * @throws AiAssistantException when the trusted tool context
     *         cannot be resolved (caller invoked the tool outside
     *         the AI request flow)
     */
    @Tool(
        name = "obtenerResumenChat",
        description = "Returns the persisted context summary for the current AI conversation. "
            + "Use this to recall the rolling facts and inferences that the assistant has "
            + "already captured about the chat. Returns an empty placeholder when no "
            + "summary has been persisted yet. Read-only — does NOT mutate any entity."
    )
    public ObtenerResumenChatResponse obtenerResumenChat() {
        AiToolContext ctx;
        try {
            ctx = aiToolContextPort.resolve();
        } catch (IllegalStateException ex) {
            throw AiAssistantException.upstreamFailure(
                "AI tool invoked without trusted AiToolContext: " + ex.getMessage(), ex);
        }

        Optional<AiResumenContexto> resumen =
            findAiResumenPort.findByConversacionId(ctx.aiConversacionId());

        if (resumen.isEmpty()) {
            log.info("AI tool fetched no summary for aiConversacionId={}", ctx.aiConversacionId());
            return new ObtenerResumenChatResponse(null, null, null, null);
        }
        AiResumenContexto r = resumen.get();
        return new ObtenerResumenChatResponse(
            r.getFacts(),
            r.getInferences(),
            r.getSourceWatermark(),
            r.getActualizadoEn()
        );
    }
}