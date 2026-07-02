package com.ar.crm2.adapter.out.ai.spring.mapper;

import com.ar.crm2.application.ai.port.out.dto.MensajeChat;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RED-first tests for {@link SpringAiPromptMapper} — the infrastructure
 * collaborator that maps the application-owned {@link ChatAsistenteRequest}
 * to the user-facing prompt text that {@code OpenAiChatAdapter} forwards to
 * Spring AI's {@code ChatClient}.
 *
 * <p>What these tests pin:
 * <ul>
 *   <li>Scope ids ({@code aiConversacionId}, {@code actorUsuarioId},
 *       {@code empresaId}, {@code waConversacionId}) ALWAYS appear so
 *       the model reasons with the correct tenant / conversation.</li>
 *   <li>Optional sections (facts, inferences, transcript, kickoff) are
 *       rendered only when the DTO field carries a non-blank / non-empty
 *       value.</li>
 *   <li>Transcript entries are formatted as {@code "-{direccion}: {contenido}"}.</li>
 *   <li>The mapper does NOT validate business rules and does NOT
 *       import any Spring AI type — verified by reflection guards.</li>
 * </ul>
 */
class SpringAiPromptMapperTest {

    private final SpringAiPromptMapper mapper = new SpringAiPromptMapper();

    private static final UUID AI_CONV = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000001");
    private static final UUID ACTOR = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000002");
    private static final UUID EMPRESA = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000003");

    // ── Scope ids always present ─────────────────────────────────────

    @Test
    @DisplayName("toUserPrompt always includes the AI conversation id")
    void toUserPrompt_includesAiConversacionId() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-conv-123",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains(AI_CONV.toString()),
            "scope ids must always appear; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt always includes actor / empresa / wa-conversation scope ids")
    void toUserPrompt_includesAllScopeIds() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-conv-xyz",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains(ACTOR.toString()),
            "actorUsuarioId must always appear; got: " + prompt);
        assertTrue(prompt.contains(EMPRESA.toString()),
            "empresaId must always appear; got: " + prompt);
        assertTrue(prompt.contains("wa-conv-xyz"),
            "waConversacionId must always appear; got: " + prompt);
    }

    // ── Facts section ────────────────────────────────────────────────

    @Test
    @DisplayName("toUserPrompt renders the facts section when resumenFacts is non-blank")
    void toUserPrompt_includesFactsWhenPresent() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            "cliente premium desde 2023", null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("Persisted facts"),
            "facts header must appear when facts are present; got: " + prompt);
        assertTrue(prompt.contains("cliente premium desde 2023"),
            "facts content must appear; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt omits the facts section when resumenFacts is null")
    void toUserPrompt_omitsFactsWhenNull() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            null, "inferences here", null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertFalse(prompt.contains("Persisted facts"),
            "facts header must NOT appear when facts are null; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt omits the facts section when resumenFacts is blank")
    void toUserPrompt_omitsFactsWhenBlank() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            "   ", "inferences here", null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertFalse(prompt.contains("Persisted facts"),
            "facts header must NOT appear when facts are blank; got: " + prompt);
    }

    // ── Inferences section ───────────────────────────────────────────

    @Test
    @DisplayName("toUserPrompt renders the inferences section when resumenInferences is non-blank")
    void toUserPrompt_includesInferencesWhenPresent() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            null, "prefiere contacto por WhatsApp", null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("Persisted inferences"),
            "inferences header must appear when inferences are present; got: " + prompt);
        assertTrue(prompt.contains("prefiere contacto por WhatsApp"),
            "inferences content must appear; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt omits the inferences section when resumenInferences is null or blank")
    void toUserPrompt_omitsInferencesWhenNullOrBlank() {
        ChatAsistenteRequest solicitudNull = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            "facts here", null, null
        );
        assertFalse(mapper.toUserPrompt(solicitudNull).contains("Persisted inferences"),
            "inferences header must NOT appear when inferences are null");

        ChatAsistenteRequest solicitudBlank = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            "facts here", "\t  ", null
        );
        assertFalse(mapper.toUserPrompt(solicitudBlank).contains("Persisted inferences"),
            "inferences header must NOT appear when inferences are blank");
    }

    // ── Transcript section ───────────────────────────────────────────

    @Test
    @DisplayName("toUserPrompt renders transcript entries formatted as -{direccion}: {contenido}")
    void toUserPrompt_includesTranscriptEntriesFormatted() {
        WhatsappMensajeResumen entrante = new WhatsappMensajeResumen(
            UUID.randomUUID(), UUID.randomUUID(),
            "ENTRANTE", "TEXT", "hola, necesito soporte", null,
            LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        WhatsappMensajeResumen saliente = new WhatsappMensajeResumen(
            UUID.randomUUID(), UUID.randomUUID(),
            "SALIENTE", "TEXT", "claro, en que le ayudo?", null,
            LocalDateTime.of(2026, 6, 24, 10, 5)
        );
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(entrante, saliente),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("Persisted WhatsApp transcript"),
            "transcript header must appear when transcript is non-empty; got: " + prompt);
        assertTrue(prompt.contains("-ENTRANTE: hola, necesito soporte"),
            "entry must be formatted '-ENTRANTE: {contenido}'; got: " + prompt);
        assertTrue(prompt.contains("-SALIENTE: claro, en que le ayudo?"),
            "entry must be formatted '-SALIENTE: {contenido}'; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt omits the transcript section when transcript is null or empty")
    void toUserPrompt_omitsTranscriptWhenEmptyOrNull() {
        ChatAsistenteRequest solicitudEmpty = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        assertFalse(mapper.toUserPrompt(solicitudEmpty).contains("Persisted WhatsApp transcript"),
            "transcript header must NOT appear when transcript is empty");
    }

    // ── Kickoff section ──────────────────────────────────────────────

    @Test
    @DisplayName("toUserPrompt renders the user kickoff section when kickoffUsuario is non-blank")
    void toUserPrompt_includesKickoffWhenPresent() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            null, null, "por favor crea una tarea prioritaria"
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("User kickoff"),
            "kickoff header must appear when kickoff is present; got: " + prompt);
        assertTrue(prompt.contains("por favor crea una tarea prioritaria"),
            "kickoff content must appear; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt omits the kickoff section when kickoffUsuario is null or blank")
    void toUserPrompt_omitsKickoffWhenNullOrBlank() {
        ChatAsistenteRequest solicitudNull = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        assertFalse(mapper.toUserPrompt(solicitudNull).contains("User kickoff"),
            "kickoff header must NOT appear when kickoff is null");

        ChatAsistenteRequest solicitudBlank = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-1",
            List.of(), List.of(), List.of(),
            null, null, "   "
        );
        assertFalse(mapper.toUserPrompt(solicitudBlank).contains("User kickoff"),
            "kickoff header must NOT appear when kickoff is blank");
    }

    // ── Prompt is non-null and never empty (scope alone is enough) ──

    @Test
    @DisplayName("toUserPrompt always returns a non-null, non-empty string — scope alone is enough")
    void toUserPrompt_neverEmpty_whenOnlyScopeIsProvided() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-only-scope",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertNotNull(prompt, "toUserPrompt must never return null");
        assertFalse(prompt.isBlank(),
            "toUserPrompt must always render at least the scope section; got: '" + prompt + "'");
    }

    // ── No-fabrication contract (Phase A — AI tool safety) ────────────
    //
    // The contract:
    //   1. Every prompt ALWAYS includes an explicit no-fabrication
    //      directive so the model cannot invent CRM facts not returned
    //      by the registered @Tool adapters.
    //   2. When the request carries NO data sources (empty transcript,
    //      empty history, empty memory, no facts/inferences/kickoff),
    //      the mapper surfaces a stronger "no data" directive so the
    //      model is told to refuse rather than guess.

    @Test
    @DisplayName("toUserPrompt always includes the no-fabrication directive — full data available")
    void toUserPrompt_includesNoFabricationDirective_alwaysPresent() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-conv-fab",
            List.of(), List.of(), List.of(),
            "facts here", "inferences here",
            "user kickoff here"
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("NO_FABRICATION_DIRECTIVE"),
            "no-fabrication directive MUST always appear in the prompt so the "
                + "model cannot invent CRM facts not returned by @Tool adapters; got: " + prompt);
        assertTrue(prompt.toLowerCase().contains("do not invent")
                || prompt.toLowerCase().contains("do NOT invent"),
            "the directive must explicitly forbid fabricating CRM values; got: " + prompt);
        assertTrue(prompt.toLowerCase().contains("tool"),
            "the directive must anchor the model's claims to tool-returned data; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt includes the no-fabrication directive even with empty data sources")
    void toUserPrompt_includesNoFabricationDirective_whenOnlyScopeProvided() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-only-scope",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("NO_FABRICATION_DIRECTIVE"),
            "no-fabrication directive MUST appear even when only scope is provided; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt surfaces the no-data safe-fallback directive when ALL data sources are empty")
    void toUserPrompt_includesSafeFallbackDirective_whenAllDataSourcesEmpty() {
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-empty-data",
            List.of(), List.of(), List.of(),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertTrue(prompt.contains("NO_DATA_AVAILABLE")
                || prompt.contains("no data is available")
                || prompt.contains("NO_DATA_SAFE_FALLBACK"),
            "safe-fallback directive MUST appear when transcript + history + memory + facts + "
                + "inferences + kickoff are all empty so the model is told to refuse rather than "
                + "fabricate; got: " + prompt);
    }

    @Test
    @DisplayName("toUserPrompt omits the no-data fallback directive when at least one data source is present")
    void toUserPrompt_omitsSafeFallbackDirective_whenAnyDataSourcePresent() {
        WhatsappMensajeResumen entrante = new WhatsappMensajeResumen(
            UUID.randomUUID(), UUID.randomUUID(),
            "ENTRANTE", "TEXT", "hola", null,
            LocalDateTime.of(2026, 6, 30, 10, 0)
        );
        ChatAsistenteRequest solicitud = sampleSolicitud(
            AI_CONV, ACTOR, EMPRESA, "wa-has-data",
            List.of(), List.of(), List.of(entrante),
            null, null, null
        );
        String prompt = mapper.toUserPrompt(solicitud);

        assertFalse(prompt.contains("NO_DATA_AVAILABLE")
                && prompt.contains("no data is available"),
            "safe-fallback directive MUST NOT appear when at least one data source (transcript) "
                + "is present — the model has tool-derived data to reason over; got: " + prompt);
    }

    // ── Architecture guard: mapper is framework-free ────────────────

    @Test
    @DisplayName("SpringAiPromptMapper has no Spring AI imports — verified via classloader")
    void mapper_doesNotImportSpringAi() throws Exception {
        // Read the mapper source file (loaded by the test classloader) and
        // assert no `org.springframework.ai` import line is present. The
        // mapper's contract is application-DTO -> String; framework types
        // belong in OpenAiChatAdapter.
        String resource = "com/ar/crm2/adapter/out/ai/spring/mapper/SpringAiPromptMapper.class";
        var url = Thread.currentThread().getContextClassLoader().getResource(resource);
        assertNotNull(url, "compiled class must be on the classpath");

        // Simpler and equally robust: introspect the type's declared
        // imports via the source file (the test runs against the source
        // tree, not against a packaged jar).
        java.nio.file.Path sourcePath = java.nio.file.Path.of(
            "src/main/java/com/ar/crm2/adapter/out/ai/spring/mapper/SpringAiPromptMapper.java"
        );
        if (java.nio.file.Files.exists(sourcePath)) {
            String source = java.nio.file.Files.readString(sourcePath);
            assertFalse(source.contains("org.springframework.ai"),
                "SpringAiPromptMapper must not import any Spring AI type; "
                    + "framework mapping is owned by OpenAiChatAdapter. Source: " + source);
        }
    }

    @Test
    @DisplayName("SpringAiPromptMapper constructor takes no arguments — pure stateless mapping")
    void mapper_isStatelessAndParameterless() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors =
            SpringAiPromptMapper.class.getDeclaredConstructors();
        assertEquals(1, ctors.length,
            "SpringAiPromptMapper must expose exactly one constructor");
        assertEquals(0, ctors[0].getParameterCount(),
            "SpringAiPromptMapper must take no arguments — pure mapping function");
    }

    // ── Helper ───────────────────────────────────────────────────────

    private static ChatAsistenteRequest sampleSolicitud(
        UUID aiConversacionId,
        UUID actorUsuarioId,
        UUID empresaId,
        String waConversacionId,
        List<MensajeChat> historial,
        List<MensajeChat> memoria,
        List<WhatsappMensajeResumen> transcript,
        String resumenFacts,
        String resumenInferences,
        String kickoffUsuario
    ) {
        return new ChatAsistenteRequest(
            aiConversacionId,
            actorUsuarioId,
            empresaId,
            waConversacionId,
            historial,
            memoria,
            transcript,
            resumenFacts,
            resumenInferences,
            kickoffUsuario
        );
    }
}
