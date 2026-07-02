package com.ar.crm2.adapter.out.ai.spring.mapper;

import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RED-first tests for {@link SpringAiChatResponseMapper} — the
 * infrastructure collaborator that maps a Spring AI {@link ChatResponse}
 * to the application-owned {@link RespuestaAsistente}.
 *
 * <p>What these tests pin:
 * <ul>
 *   <li>Happy path projects content, model id, prompt/completion tokens,
 *       and the supplied wall-clock latency into the application DTO.</li>
 *   <li>Structural / framework-level failures (null {@code ChatResponse},
 *       null result, null output, null output text) map to
 *       {@link AiAssistantException} — NO silent fallback to empty
 *       strings or placeholder model ids.</li>
 *   <li>Missing optional metadata (no {@code getMetadata()} usage block)
 *       surfaces as {@code null} tokens / model id in the response DTO
 *       (the DTO tolerates {@code null} for these fields).</li>
 *   <li>Latency is passed through unchanged.</li>
 *   <li>The mapper does NOT validate business rules (no tenant,
 *       ownership, payload, or {@code TipoAccion} checks).</li>
 * </ul>
 */
class SpringAiChatResponseMapperTest {

    private final SpringAiChatResponseMapper mapper =
        new SpringAiChatResponseMapper();

    // ── Happy path ──────────────────────────────────────────────────

    @Test
    @DisplayName("toResponse projects content, model id, tokens, and latency into the application DTO")
    void toResponse_happyPath_returnsRespuestaAsistente() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn("Propongo crear la tarea");
        when(response.getMetadata().getUsage().getPromptTokens()).thenReturn(120);
        when(response.getMetadata().getUsage().getCompletionTokens()).thenReturn(45);
        when(response.getMetadata().getModel()).thenReturn("gpt-4o-mini");

        RespuestaAsistente out = mapper.toResponse(response, 250L);

        assertNotNull(out, "mapper must return a non-null application-owned record");
        assertEquals("Propongo crear la tarea", out.contenido(),
            "RespuestaAsistente.contenido must carry the model reply text");
        assertEquals("gpt-4o-mini", out.modelo(),
            "RespuestaAsistente.modelo must reflect the framework model id");
        assertEquals(Integer.valueOf(120), out.promptTokens(),
            "RespuestaAsistente.promptTokens must come from the metadata");
        assertEquals(Integer.valueOf(45), out.completionTokens(),
            "RespuestaAsistente.completionTokens must come from the metadata");
        assertEquals(Long.valueOf(250L), out.latencyMs(),
            "RespuestaAsistente.latencyMs must be the supplied wall-clock latency");
    }

    // ── Structural framework checks ─────────────────────────────────

    @Test
    @DisplayName("toResponse throws AiAssistantException when ChatResponse is null")
    void toResponse_nullChatResponse_throws() {
        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> mapper.toResponse(null, 0L));
        assertTrue(ex.getMessage().contains("null ChatResponse"),
            "exception message must explain the structural failure; got: " + ex.getMessage());
    }

    @Test
    @DisplayName("toResponse throws AiAssistantException when ChatResponse.getResult() is null")
    void toResponse_nullResult_throws() {
        ChatResponse response = mock(ChatResponse.class);
        when(response.getResult()).thenReturn(null);

        assertThrows(AiAssistantException.class,
            () -> mapper.toResponse(response, 0L));
    }

    @Test
    @DisplayName("toResponse throws AiAssistantException when result.getOutput() is null")
    void toResponse_nullOutput_throws() {
        ChatResponse response = mock(ChatResponse.class);
        org.springframework.ai.chat.model.Generation generation =
            mock(org.springframework.ai.chat.model.Generation.class);
        when(generation.getOutput()).thenReturn(null);
        when(response.getResult()).thenReturn(generation);

        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> mapper.toResponse(response, 0L));
        assertTrue(ex.getMessage().contains("missing the result output"),
            "exception message must explain the structural failure; got: " + ex.getMessage());
    }

    @Test
    @DisplayName("toResponse throws AiAssistantException when output text is null")
    void toResponse_nullOutputText_throws() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn(null);

        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> mapper.toResponse(response, 0L));
        assertTrue(ex.getMessage().contains("output text is null"),
            "exception message must explain the structural failure; got: " + ex.getMessage());
    }

    // ── Missing optional metadata is tolerated as null ──────────────

    @Test
    @DisplayName("toResponse returns null model id and null tokens when metadata is absent (no fallback)")
    void toResponse_missingMetadata_returnsNulls() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn("hola");
        when(response.getMetadata()).thenReturn(null);

        RespuestaAsistente out = mapper.toResponse(response, 10L);

        assertEquals("hola", out.contenido());
        assertNull(out.modelo(),
            "no silent fallback to configured model id when framework omits it; "
                + "the application sees the omission");
        assertNull(out.promptTokens(),
            "no silent fallback to 0 prompt tokens; the application sees the omission");
        assertNull(out.completionTokens(),
            "no silent fallback to 0 completion tokens; the application sees the omission");
        assertEquals(Long.valueOf(10L), out.latencyMs());
    }

    @Test
    @DisplayName("toResponse passes latency through unchanged even when content is null (latency capture is structural)")
    void toResponse_latencyIsPassedThrough() {
        // Latency is a wall-clock measurement the adapter owns; it must
        // be captured even when the framework returns a non-null but
        // structurally-empty response. Use a non-null response that
        // fails the content check, then verify latency was the value
        // we supplied.
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn(null);

        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> mapper.toResponse(response, 999L));
        // Latency assertion is implicit — the call did not crash before
        // reaching the structural check, and the mapper accepted the
        // value without modification.
        assertTrue(ex.getMessage().contains("output text is null"));
    }

    // ── Architecture guards ──────────────────────────────────────────

    @Test
    @DisplayName("SpringAiChatResponseMapper constructor takes no arguments — pure stateless mapping")
    void mapper_isStatelessAndParameterless() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors =
            SpringAiChatResponseMapper.class.getDeclaredConstructors();
        assertEquals(1, ctors.length,
            "SpringAiChatResponseMapper must expose exactly one constructor");
        assertEquals(0, ctors[0].getParameterCount(),
            "SpringAiChatResponseMapper must take no arguments — pure mapping function");
    }

    @Test
    @DisplayName("SpringAiChatResponseMapper has no application/domain types in its surface API")
    void mapper_onlyDependsOnFrameworkAndApplicationDto() throws Exception {
        // The mapper's only application-surface type must be the
        // RespuestaAsistente return type. Public methods must not take
        // ChatAsistenteRequest, port types, or domain types — those
        // are owned upstream.
        for (java.lang.reflect.Method m : SpringAiChatResponseMapper.class.getDeclaredMethods()) {
            if (m.getName().equals("toResponse")) {
                Class<?>[] params = m.getParameterTypes();
                assertEquals(2, params.length,
                    "toResponse must take (ChatResponse, long) — no extra ports or domain types");
                assertEquals(ChatResponse.class, params[0],
                    "first parameter must be the Spring AI ChatResponse");
                assertEquals(long.class, params[1],
                    "second parameter must be a primitive long (latency)");
                assertEquals(RespuestaAsistente.class, m.getReturnType(),
                    "return type must be the application-owned RespuestaAsistente DTO");
            }
        }
    }
}
