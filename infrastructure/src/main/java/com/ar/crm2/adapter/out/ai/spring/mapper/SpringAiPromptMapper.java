package com.ar.crm2.adapter.out.ai.spring.mapper;

import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;

import java.util.List;

/**
 * Pure mapping function from the application-owned
 * {@link ChatAsistenteRequest} to the user-facing prompt text that
 * {@link OpenAiChatAdapter} forwards to Spring AI's
 * {@code ChatClient}.
 *
 * <p>This collaborator is intentionally narrow:
 * <ul>
 *   <li>Scope ids ({@code aiConversacionId}, {@code actorUsuarioId},
 *       {@code empresaId}, {@code waConversacionId}) are always
 *       rendered so the model reasons with the correct tenant /
 *       conversation.</li>
 *   <li>Optional sections (facts, inferences, transcript, kickoff) are
 *       rendered only when the corresponding DTO field carries a
 *       non-blank / non-empty value.</li>
 *   <li>Transcript entries are formatted as
 *       {@code "-{direccion}: {contenido}"}.</li>
 *   <li>The prompt ALWAYS includes an explicit
 *       {@code NO_FABRICATION_DIRECTIVE} so the model cannot invent CRM
 *       facts not returned by the registered {@code @Tool} adapters
 *       (Requirement: Tool-Only Data Access in
 *       {@code specs/ai-assistant/spec.md}).</li>
 *   <li>When every data source is empty/null, the prompt surfaces a
 *       stronger {@code NO_DATA_AVAILABLE} safe-fallback directive so
 *       the model refuses rather than guesses — fulfilling the
 *       "No tool covers the question" scenario.</li>
 * </ul>
 *
 * <p><b>No business validation lives here.</b> Scope ids are
 * forwarded as-is from the application DTO (which itself was
 * validated upstream by the application service). The mapper does
 * NOT check tenant ownership, payload shape, AI action semantics,
 * or any other business rule.
 *
 * <p><b>No Spring AI imports.</b> The mapper depends only on
 * application-owned DTOs; framework types are confined to
 * {@code OpenAiChatAdapter}.
 */
public class SpringAiPromptMapper {

    /**
     * Directive marker: every prompt the mapper produces contains this
     * marker so a verifier (test or downstream tool) can prove the
     * no-fabrication contract is in force without parsing prose.
     */
    static final String NO_FABRICATION_MARKER = "NO_FABRICATION_DIRECTIVE";

    /**
     * Safe-fallback directive marker: surfaced when every data source
     * (transcript, history, memory, facts, inferences, kickoff) is
     * empty/null. Tells the model to refuse rather than guess.
     */
    static final String NO_DATA_MARKER = "NO_DATA_AVAILABLE";

    /**
     * Builds the user-facing prompt text.
     *
     * @param solicitud the application-owned request DTO — never null
     *                  (the DTO's compact constructor rejects null
     *                  scope ids upstream).
     * @return the prompt text forwarded to Spring AI's
     *         {@code ChatClient.user(...)}.
     */
    public String toUserPrompt(ChatAsistenteRequest solicitud) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AI conversation id: ").append(solicitud.aiConversacionId()).append('\n');
        sb.append("Actor usuario id: ").append(solicitud.actorUsuarioId()).append('\n');
        sb.append("Empresa id: ").append(solicitud.empresaId()).append('\n');
        sb.append("WhatsApp conversation id: ").append(solicitud.waConversacionId()).append('\n');

        // Always emit the no-fabrication directive. The model is
        // explicitly forbidden from inventing CRM facts not returned
        // by the registered @Tool adapters. Every assertion the model
        // makes about CRM data MUST cite the tool-returned provenance.
        appendNoFabricationDirective(sb);

        if (hasText(solicitud.resumenFacts())) {
            sb.append("\nPersisted facts:\n").append(solicitud.resumenFacts()).append('\n');
        }
        if (hasText(solicitud.resumenInferences())) {
            sb.append("\nPersisted inferences:\n").append(solicitud.resumenInferences()).append('\n');
        }
        appendTranscript(sb, solicitud.transcript());
        if (hasText(solicitud.kickoffUsuario())) {
            sb.append("\nUser kickoff:\n").append(solicitud.kickoffUsuario()).append('\n');
        }

        // When the request carries NO data sources at all, surface the
        // safe-fallback directive so the model is told to refuse rather
        // than guess CRM values. This is the "no tool covers the
        // question" scenario from specs/ai-assistant/spec.md.
        if (!hasAnyDataSource(solicitud)) {
            appendNoDataSafeFallback(sb);
        }

        return sb.toString();
    }

    private static void appendNoFabricationDirective(StringBuilder sb) {
        sb.append("\n").append(NO_FABRICATION_MARKER).append(":\n");
        sb.append("- You MUST only assert CRM facts that are returned by a registered ")
          .append("@Tool adapter in this turn.\n");
        sb.append("- Do NOT invent, guess, or extrapolate contact / ficha / tablero / ")
          .append("conversation values that are not in the tool output above.\n");
        sb.append("- If no tool covers the user's question, respond that the data cannot ")
          .append("be retrieved rather than fabricating values.\n");
    }

    private static void appendNoDataSafeFallback(StringBuilder sb) {
        sb.append("\n").append(NO_DATA_MARKER).append(":\n");
        sb.append("- No transcript, history, memory, facts, inferences, or user kickoff ")
          .append("is available for this turn.\n");
        sb.append("- Respond that you cannot retrieve CRM data right now and ask the ")
          .append("user to supply more context or retry once the data is available.\n");
        sb.append("- Do NOT fabricate any CRM value (id, name, phone, status, etc.).\n");
    }

    private static boolean hasAnyDataSource(ChatAsistenteRequest solicitud) {
        if (hasText(solicitud.resumenFacts())) return true;
        if (hasText(solicitud.resumenInferences())) return true;
        if (hasText(solicitud.kickoffUsuario())) return true;
        if (solicitud.transcript() != null && !solicitud.transcript().isEmpty()) return true;
        if (solicitud.historial() != null && !solicitud.historial().isEmpty()) return true;
        if (solicitud.memoria() != null && !solicitud.memoria().isEmpty()) return true;
        return false;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static void appendTranscript(StringBuilder sb,
                                         List<WhatsappMensajeResumen> t) {
        if (t == null || t.isEmpty()) {
            return;
        }
        sb.append("\nPersisted WhatsApp transcript:\n");
        for (WhatsappMensajeResumen m : t) {
            sb.append('-').append(m.direccion()).append(": ")
              .append(m.contenido()).append('\n');
        }
    }
}
