package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerResumenChatResponse;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.annotation.Tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * RED-first tests for {@link ObtenerResumenChatTool} — the read-only
 * {@code @Tool} input adapter that returns the persisted
 * {@code AiResumenContexto} for the current AI conversation.
 *
 * <p>The tool takes NO parameters — the {@code aiConversacionId} is
 * resolved from the trusted {@link AiToolContextPort} so the model
 * cannot spoof a foreign conversation.
 *
 * <p>If no context is bound (the tool was invoked outside the AI
 * request flow) the tool throws {@link AiAssistantException} — the
 * same way the upstream {@link AiToolContextPort} surfaces missing
 * context. The tool MUST NOT silently return an empty summary.
 */
@ExtendWith(MockitoExtension.class)
class ObtenerResumenChatToolTest {

    @Mock
    private AiToolContextPort aiToolContextPort;

    @Mock
    private FindAiResumenPort findAiResumenPort;

    private ObtenerResumenChatTool tool;

    @BeforeEach
    void setUp() {
        tool = new ObtenerResumenChatTool(aiToolContextPort, findAiResumenPort);
    }

    @Test
    @DisplayName("obtenerResumenChat returns the persisted summary for the trusted aiConversacionId")
    void obtenerResumenChat_returnsSummaryForTrustedConv() {
        UUID actor = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000010");
        UUID empresa = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000020");
        UUID aiConv = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000030");
        String waConv = "aaaaaaaa-1111-2222-3333-000000000040";
        AiToolContext ctx = new AiToolContext(actor, empresa, aiConv, waConv);
        when(aiToolContextPort.resolve()).thenReturn(ctx);

        AiResumenContexto resumen = AiResumenContexto.reconstitute(
            com.ar.crm2.model.vo.AiResumenContextoId.from(
                UUID.fromString("11111111-2222-3333-4444-555555555555")),
            UsuarioId.from(actor),
            EmpresaId.from(empresa),
            waConv,
            ContactoId.from(UUID.fromString("aaaaaaaa-1111-2222-3333-000000000050")),
            "Cliente quiere demo",
            "Inferencia: presupuesto aprobado",
            UUID.fromString("aaaaaaaa-1111-2222-3333-000000000060").toString(),
            3L,
            com.ar.crm2.model.vo.AiConversacionId.from(aiConv),
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 2, 0, 0)
        );
        when(findAiResumenPort.findByConversacionId(aiConv)).thenReturn(java.util.Optional.of(resumen));

        ObtenerResumenChatResponse out = tool.obtenerResumenChat();

        assertNotNull(out);
        assertEquals("Cliente quiere demo", out.facts());
        assertEquals("Inferencia: presupuesto aprobado", out.inferences());
        assertEquals(Long.valueOf(3L), out.sourceWatermark());
        assertEquals(LocalDateTime.of(2026, 1, 2, 0, 0), out.actualizadoEn());
    }

    @Test
    @DisplayName("obtenerResumenChat returns an empty placeholder when the conversation has no summary yet")
    void obtenerResumenChat_emptyWhenNoSummary() {
        UUID actor = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000010");
        UUID empresa = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000020");
        UUID aiConv = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000030");
        AiToolContext ctx = new AiToolContext(actor, empresa, aiConv, "aaaaaaaa-1111-2222-3333-000000000040");
        when(aiToolContextPort.resolve()).thenReturn(ctx);
        when(findAiResumenPort.findByConversacionId(aiConv)).thenReturn(java.util.Optional.empty());

        ObtenerResumenChatResponse out = tool.obtenerResumenChat();

        assertNotNull(out, "the tool must never return null — empty placeholder instead");
        assertNull(out.facts(), "no summary yet -> facts is null");
        assertNull(out.inferences(), "no summary yet -> inferences is null");
        assertNull(out.sourceWatermark(), "no summary yet -> sourceWatermark is null");
        assertNull(out.actualizadoEn(), "no summary yet -> actualizadoEn is null");
    }

    @Test
    @DisplayName("obtenerResumenChat throws AiAssistantException when no tool context is bound")
    void obtenerResumenChat_throwsWhenNoContext() {
        when(aiToolContextPort.resolve())
            .thenThrow(new IllegalStateException("no context"));

        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> tool.obtenerResumenChat());
        assertNotNull(ex.getCause(),
            "the IllegalStateException from the port must be preserved as the cause");
    }

    @Test
    @DisplayName("@Tool method is annotated and takes no parameters (scope comes from AiToolContext)")
    void toolMethod_isAnnotatedAndParameterless() throws NoSuchMethodException {
        Method m = ObtenerResumenChatTool.class.getDeclaredMethod("obtenerResumenChat");
        Tool toolAnn = m.getAnnotation(Tool.class);
        assertNotNull(toolAnn, "@Tool annotation required so Spring AI registers the tool");
        assertFalse(toolAnn.description().isBlank(),
            "@Tool description must be non-blank so the model knows when to call it");
        assertEquals("obtenerResumenChat", toolAnn.name(),
            "tool name must be deterministic and intention-revealing");
        assertEquals(0, m.getParameterCount(),
            "obtenerResumenChat must take no parameters — aiConversacionId is resolved "
                + "from the trusted AiToolContextPort so the model cannot spoof a foreign conversation");
    }

    @Test
    @DisplayName("Constructor declares ONLY the trusted scope port + the read port — no mutation use cases")
    void constructor_doesNotInjectRealMutationUseCases() {
        Constructor<?>[] ctors = ObtenerResumenChatTool.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Class<?>[] params = ctors[0].getParameterTypes();
        assertEquals(2, params.length,
            "tool must take exactly 2 collaborators: AiToolContextPort + FindAiResumenPort");
        assertEquals(AiToolContextPort.class, params[0],
            "first param must be AiToolContextPort — scope resolution");
        assertEquals(FindAiResumenPort.class, params[1],
            "second param must be FindAiResumenPort — read access to AiResumenContexto");
        // Field-level guard against accidental injection
        for (Class<?> forbidden : List.of(
                CreateContactoUseCase.class, CreateTratoUseCase.class,
                CreateTareaUseCase.class, MoverColumnaFichaUseCase.class)) {
            for (Field f : ObtenerResumenChatTool.class.getDeclaredFields()) {
                assertFalse(f.getType().equals(forbidden),
                    "field '" + f.getName() + "' leaks " + forbidden.getSimpleName()
                        + " — read-only tools must never mutate");
            }
        }
    }

    @Test
    @DisplayName("Tool class does not hold DTOs / projections as fields (delegation only)")
    void toolClass_doesNotHoldDtoConstructionFields() {
        for (Field f : ObtenerResumenChatTool.class.getDeclaredFields()) {
            assertFalse(f.getType().equals(ObtenerResumenChatResponse.class),
                "tool must not hold the response DTO as a field; responses are built per-call");
            assertFalse(f.getType().equals(AiResumenContexto.class),
                "tool must not hold the port projection as a field; projections are read per-call");
            assertFalse(f.getType().equals(AiToolContext.class),
                "tool must not hold the context as a field; it is resolved per-call from the port");
        }
    }

    @Test
    @DisplayName("AiConversacionId is fetched from the trusted context, never from the model payload")
    void aiConversacionId_isSourcedFromTrustedContext() throws NoSuchMethodException {
        // Verifies the security boundary: even if the model tried to
        // spoof a foreign aiConversacionId, it cannot — the tool
        // pulls the id from the trusted port resolution.
        UUID trustedAiConv = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000099");
        AiToolContext ctx = new AiToolContext(
            UUID.fromString("aaaaaaaa-1111-2222-3333-000000000010"),
            UUID.fromString("aaaaaaaa-1111-2222-3333-000000000020"),
            trustedAiConv,
            "aaaaaaaa-1111-2222-3333-000000000040"
        );
        when(aiToolContextPort.resolve()).thenReturn(ctx);
        when(findAiResumenPort.findByConversacionId(trustedAiConv))
            .thenReturn(java.util.Optional.empty());

        // The tool has no parameters, so the only way to verify scope
        // is to assert that the trusted id is what reaches the port.
        tool.obtenerResumenChat();
        org.mockito.Mockito.verify(findAiResumenPort)
            .findByConversacionId(trustedAiConv);
        // Confirm the @Tool method declares no parameters.
        Method toolMethod = ObtenerResumenChatTool.class.getDeclaredMethod("obtenerResumenChat");
        assertEquals(0, toolMethod.getParameterCount(),
            "the @Tool method must take 0 parameters — aiConversacionId is resolved "
                + "from the trusted AiToolContextPort, not the model payload");
    }
}