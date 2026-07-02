package com.ar.crm2.adapter.out.ai.spring.mapper;

import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * Pure mapping function from a Spring AI {@link ChatResponse} to the
 * application-owned {@link RespuestaAsistente}.
 *
 * <p>This collaborator is intentionally narrow:
 * <ul>
 *   <li>Performs only structural / framework-level checks
 *       (null {@link ChatResponse}, null result, null output,
 *       null output text). These are mapped to the
 *       application-owned {@link AiAssistantException} — NO silent
 *       fallback to empty strings or placeholder model ids.</li>
 *   <li>Missing optional metadata (no {@code getMetadata()} usage
 *       block) surfaces as {@code null} tokens / model id in the
 *       response DTO (the DTO tolerates {@code null} for these
 *       fields). This lets the application see the omission rather
 *       than masking it as {@code 0} tokens or the configured
 *       fallback model id.</li>
 *   <li>Latency is passed through unchanged — the adapter owns
 *       wall-clock latency capture, the mapper does not.</li>
 * </ul>
 *
 * <p><b>No business validation lives here.</b> The mapper does
 * NOT check tenant ownership, payload shape, AI action semantics
 * ({@code TipoAccion}), or any other business rule. Tool-driven
 * proposals are not produced here — the application service owns
 * their staging via {@code ProponerAccionUseCase}.
 */
public class SpringAiChatResponseMapper {

    /**
     * Projects a {@link ChatResponse} to an application-owned
     * {@link RespuestaAsistente}.
     *
     * @param response the Spring AI response — structural nulls are
     *                 mapped to {@link AiAssistantException}.
     * @param latencyMs the wall-clock latency captured by the adapter
     *                  — passed through to the response DTO.
     * @return the application-owned response, never null.
     * @throws AiAssistantException when the framework response
     *         cannot be faithfully projected to a
     *         {@link RespuestaAsistente}.
     */
    public RespuestaAsistente toResponse(ChatResponse response, long latencyMs) {
        if (response == null) {
            throw AiAssistantException.upstreamFailure(
                "ChatClient returned a null ChatResponse"
            );
        }
        if (response.getResult() == null
                || response.getResult().getOutput() == null) {
            throw AiAssistantException.upstreamFailure(
                "ChatResponse is missing the result output — cannot extract content"
            );
        }
        String content = response.getResult().getOutput().getText();
        if (content == null) {
            throw AiAssistantException.upstreamFailure(
                "ChatResponse output text is null — cannot faithfully project content"
            );
        }

        // Optional metadata: the framework may omit it. We surface the
        // omission as null rather than substituting a silent fallback,
        // so the application sees exactly what the framework returned.
        return new RespuestaAsistente(
            content,
            extractModelId(response),
            extractPromptTokens(response),
            extractCompletionTokens(response),
            latencyMs
        );
    }

    private static Integer extractPromptTokens(ChatResponse response) {
        Usage usage = extractUsage(response);
        return usage == null ? null : usage.getPromptTokens();
    }

    private static Integer extractCompletionTokens(ChatResponse response) {
        Usage usage = extractUsage(response);
        return usage == null ? null : usage.getCompletionTokens();
    }

    private static Usage extractUsage(ChatResponse response) {
        if (response.getMetadata() == null) {
            return null;
        }
        return response.getMetadata().getUsage();
    }

    private static String extractModelId(ChatResponse response) {
        if (response.getMetadata() == null) {
            return null;
        }
        return response.getMetadata().getModel();
    }
}
