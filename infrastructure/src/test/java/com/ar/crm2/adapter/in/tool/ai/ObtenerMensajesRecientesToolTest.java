package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesResponse;
import com.ar.crm2.application.ai.exception.AiAssistantException;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RED-first tests for {@link ObtenerMensajesRecientesTool} — the
 * read-only {@code @Tool} input adapter that returns the last N
 * WhatsApp messages of the conversation the AI assistant is bound to.
 *
 * <p><b>Trust boundary.</b> the {@code waConversacionId} is resolved
 * from the trusted {@link AiToolContextPort}; the model payload
 * carries only {@code limit}. The model cannot probe a foreign
 * conversation because the request DTO has no {@code waConversacionId}
 * field — the conversation id is not part of the wire contract.
 *
 * <p>The tool is intentionally narrow:
 * <ul>
 *   <li>It depends ONLY on {@link AiToolContextPort} +
 *       {@link WhatsappMensajeLecturaPort}.</li>
 *   <li>It MUST NOT depend on any real CRM mutation use case
 *       ({@code CreateContactoUseCase}, {@code CreateTratoUseCase},
 *       {@code CreateTareaUseCase}, {@code MoverColumnaFichaUseCase}).</li>
 *   <li>It caps the result to {@code limit} messages (the model
 *       controls the cap).</li>
 *   <li>If no {@link AiToolContext} is bound (the tool was invoked
 *       outside the AI request flow), the tool throws
 *       {@link AiAssistantException} so the call fails safely rather
 *       than leaking scope.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ObtenerMensajesRecientesToolTest {

    private static final UUID TRUSTED_ACTOR = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000010");
    private static final UUID TRUSTED_EMPRESA = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000020");
    private static final UUID TRUSTED_AI_CONV = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000030");
    private static final String TRUSTED_WA_CONV = "aaaaaaaa-1111-2222-3333-000000000040";

    @Mock
    private AiToolContextPort aiToolContextPort;

    @Mock
    private WhatsappMensajeLecturaPort whatsappMensajeLecturaPort;

    private ObtenerMensajesRecientesTool tool;

    @BeforeEach
    void setUp() {
        tool = new ObtenerMensajesRecientesTool(aiToolContextPort, whatsappMensajeLecturaPort);
    }

    /** A trusted AiToolContext built from the upstream request, never from the model payload. */
    private AiToolContext trustedContext() {
        return new AiToolContext(
            TRUSTED_ACTOR, TRUSTED_EMPRESA, TRUSTED_AI_CONV, TRUSTED_WA_CONV
        );
    }

    @Test
    @DisplayName("obtenerMensajesRecientes uses the trusted waConversacionId from AiToolContext (not the model payload)")
    void obtenerMensajesRecientes_usesTrustedWaConversacionIdFromContext() {
        when(aiToolContextPort.resolve()).thenReturn(trustedContext());
        UUID expectedWaConvId = UUID.fromString(TRUSTED_WA_CONV);
        List<WhatsappMensajeResumen> sample = List.of(
            resumen("m1", "INBOUND", "hola"),
            resumen("m2", "OUTBOUND", "buenas"),
            resumen("m3", "INBOUND", "tengo una pregunta")
        );
        when(whatsappMensajeLecturaPort.findByConversacionId(any(UUID.class))).thenReturn(sample);

        // The request DTO carries ONLY the limit; waConversacionId is intentionally
        // absent so the model cannot supply one.
        List<ObtenerMensajesRecientesResponse> result =
            tool.obtenerMensajesRecientes(new ObtenerMensajesRecientesRequest(10));

        assertNotNull(result);
        assertEquals(3, result.size());
        assertNotNull(result.get(0).id());
        assertEquals("hola", result.get(0).contenido());
        assertEquals("INBOUND", result.get(0).direccion());

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(whatsappMensajeLecturaPort).findByConversacionId(captor.capture());
        assertEquals(expectedWaConvId, captor.getValue(),
            "the tool MUST source waConversacionId from the trusted AiToolContextPort, "
                + "NOT from the model payload — there is no waConversacionId in the request DTO");
    }

    @Test
    @DisplayName("obtenerMensajesRecientes caps the result to the model-supplied limit (last N)")
    void obtenerMensajesRecientes_capsToLimit() {
        when(aiToolContextPort.resolve()).thenReturn(trustedContext());
        List<WhatsappMensajeResumen> all = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            all.add(resumen("m" + i, "INBOUND", "contenido " + i));
        }
        when(whatsappMensajeLecturaPort.findByConversacionId(any(UUID.class))).thenReturn(all);

        List<ObtenerMensajesRecientesResponse> result = tool.obtenerMensajesRecientes(
            new ObtenerMensajesRecientesRequest(3));

        assertEquals(3, result.size(),
            "limit=3 must cap the result to exactly 3 messages");
    }

    @Test
    @DisplayName("obtenerMensajesRecientes returns empty list when the port returns empty")
    void obtenerMensajesRecientes_emptyPortReturnsEmpty() {
        when(aiToolContextPort.resolve()).thenReturn(trustedContext());
        when(whatsappMensajeLecturaPort.findByConversacionId(any(UUID.class))).thenReturn(List.of());

        List<ObtenerMensajesRecientesResponse> result = tool.obtenerMensajesRecientes(
            new ObtenerMensajesRecientesRequest(10));

        assertNotNull(result);
        assertTrue(result.isEmpty(),
            "an empty port result must propagate as an empty list — never null");
    }

    @Test
    @DisplayName("obtenerMensajesRecientes throws AiAssistantException when no AiToolContext is bound")
    void obtenerMensajesRecientes_throwsAiAssistantExceptionWhenContextMissing() {
        when(aiToolContextPort.resolve())
            .thenThrow(new IllegalStateException("no AiToolContext bound on this thread"));

        AiAssistantException ex = assertThrows(AiAssistantException.class,
            () -> tool.obtenerMensajesRecientes(new ObtenerMensajesRecientesRequest(10)));
        assertNotNull(ex.getCause(),
            "the IllegalStateException from the port must be preserved as the cause");
        assertTrue(ex.getCause() instanceof IllegalStateException,
            "cause must be the IllegalStateException thrown by AiToolContextPort.resolve()");

        // Critical: the read port MUST NOT be invoked when the trusted context is missing —
        // we cannot fall back to a model-supplied waConversacionId (and there is none anyway).
        verify(whatsappMensajeLecturaPort, never()).findByConversacionId(any(UUID.class));
    }

    @Test
    @DisplayName("@Tool method is annotated so Spring AI can register it")
    void toolMethod_isAnnotated() throws NoSuchMethodException {
        Method m = ObtenerMensajesRecientesTool.class.getDeclaredMethod(
            "obtenerMensajesRecientes", ObtenerMensajesRecientesRequest.class);
        Tool toolAnn = m.getAnnotation(Tool.class);
        assertNotNull(toolAnn, "@Tool annotation is required so Spring AI registers it");
        assertFalse(toolAnn.description().isBlank(),
            "@Tool description must be non-blank so the model knows when to invoke it");
        assertEquals("obtenerMensajesRecientes", toolAnn.name(),
            "tool name must be deterministic and intention-revealing");
    }

    @Test
    @DisplayName("Request DTO is a separate class in dto/ — never nested in the tool class")
    void requestDto_isSeparateClassNotNested() {
        for (Class<?> c : ObtenerMensajesRecientesTool.class.getDeclaredClasses()) {
            assertFalse(c.getSimpleName().equals("Request"),
                "Request must be extracted to "
                    + "com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesRequest");
            assertFalse(c.getSimpleName().equals("Response"),
                "Response must be extracted to "
                    + "com.ar.crm2.adapter.in.tool.ai.dto.ObtenerMensajesRecientesResponse");
        }
    }

    @Test
    @DisplayName("Request DTO carries exactly one @ToolParam component (limit) — waConversacionId is NOT on the wire")
    void requestRecord_carriesOnlyLimitOnWire() {
        Constructor<?>[] ctors = ObtenerMensajesRecientesRequest.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Parameter[] params = ctors[0].getParameters();
        assertEquals(1, params.length,
            "Request must expose exactly 1 component (limit) — waConversacionId must NOT be on "
                + "the wire because it comes from the trusted AiToolContext, not from the model payload");
        assertEquals("limit", params[0].getName());
        ToolParam ann = params[0].getAnnotation(ToolParam.class);
        assertNotNull(ann,
            "every component must be annotated with @ToolParam; missing on " + params[0].getName());
        assertFalse(ann.description().isBlank(),
            "@ToolParam on " + params[0].getName() + " must carry a non-blank description");
    }

    @Test
    @DisplayName("Constructor declares ONLY the trusted scope port + the read port — no mutation use cases")
    void constructor_doesNotInjectRealMutationUseCases() {
        Constructor<?>[] ctors = ObtenerMensajesRecientesTool.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Class<?>[] paramTypes = ctors[0].getParameterTypes();
        assertEquals(2, paramTypes.length,
            "tool must take exactly 2 collaborators: AiToolContextPort + WhatsappMensajeLecturaPort");
        assertEquals(AiToolContextPort.class, paramTypes[0],
            "first param must be AiToolContextPort — trusted scope resolution");
        assertEquals(WhatsappMensajeLecturaPort.class, paramTypes[1],
            "second param must be WhatsappMensajeLecturaPort — read access to WhatsApp messages");
        // Make sure no mutation use case leaks in via a future edit
        for (Class<?> forbidden : List.of(
                CreateContactoUseCase.class, CreateTratoUseCase.class,
                CreateTareaUseCase.class, MoverColumnaFichaUseCase.class)) {
            for (Field f : ObtenerMensajesRecientesTool.class.getDeclaredFields()) {
                assertFalse(f.getType().equals(forbidden),
                    "field '" + f.getName() + "' leaks " + forbidden.getSimpleName()
                        + " — read-only tools must never mutate");
            }
        }
    }

    @Test
    @DisplayName("Tool class does not hold DTOs, projections, or the AiToolContext as fields (delegation only)")
    void toolClass_doesNotHoldDtoConstructionFields() {
        for (Field f : ObtenerMensajesRecientesTool.class.getDeclaredFields()) {
            assertFalse(f.getType().equals(ObtenerMensajesRecientesRequest.class),
                "tool must not hold the request DTO as a field; DTOs are supplied per-call");
            assertFalse(f.getType().equals(ObtenerMensajesRecientesResponse.class),
                "tool must not hold the response DTO as a field; responses are built per-call");
            assertFalse(f.getType().equals(WhatsappMensajeResumen.class),
                "tool must not hold the port projection as a field; projections are read per-call");
            assertFalse(f.getType().equals(AiToolContext.class),
                "tool must not hold the context as a field; it is resolved per-call from the port");
        }
    }

    @Test
    @DisplayName("The waConversacionId cannot be supplied via the request DTO — the field does not exist")
    void requestRecord_hasNoWaConversacionIdComponent() {
        // Belt-and-braces: even if a future refactor re-adds the field, this test will
        // fail and force the reviewer to acknowledge the trust boundary regression.
        Constructor<?>[] ctors = ObtenerMensajesRecientesRequest.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Parameter[] params = ctors[0].getParameters();
        for (Parameter p : params) {
            assertFalse("waConversacionId".equals(p.getName()),
                "Request DTO MUST NOT carry waConversacionId — it comes from the trusted "
                    + "AiToolContextPort, never from the model payload");
        }
        for (java.lang.reflect.RecordComponent rc : ObtenerMensajesRecientesRequest.class.getRecordComponents()) {
            assertFalse("waConversacionId".equals(rc.getName()),
                "Request DTO MUST NOT declare a waConversacionId record component — the model "
                    + "must not be able to spoof the conversation it is reading");
        }
    }

    private static WhatsappMensajeResumen resumen(String id, String direccion, String contenido) {
        return new WhatsappMensajeResumen(
            UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", Math.abs(id.hashCode()) % 1000000000000L)),
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            direccion,
            "TEXT",
            contenido,
            null,
            LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }
}