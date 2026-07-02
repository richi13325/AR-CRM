package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionRequest;
import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.application.ai.port.in.ProponerAccionUseCase;
import com.ar.crm2.application.ai.port.in.RegistrarAccionUseCase;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.model.enums.TipoAccion;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RED-first tests for {@link ProponerAccionTool} — the
 * {@code @Tool}-annotated input adapter that stages AI action
 * proposals.
 *
 * <p>The tool safety boundary is the headline invariant: AI tool
 * calls MUST go through ONE inbound use case
 * ({@link ProponerAccionUseCase}) and NEVER through real mutation
 * use cases. These tests prove the boundary by:
 * <ul>
 *   <li>Asserting the tool calls
 *       {@code ProponerAccionUseCase.proponer(...)} with the
 *       model-supplied payload fields (no scope fields — the use
 *       case resolves them through its outbound port).</li>
 *   <li>Inspecting the constructor via reflection so a future
 *       change that injects {@code CreateContactoUseCase},
 *       {@code CreateTratoUseCase}, {@code CreateTareaUseCase}, or
 *       {@code MoverColumnaFichaUseCase} fails the test before
 *       review.</li>
 *   <li>Asserting the {@code ProponerAccionRequest} DTO carries
 *       {@code @ToolParam} metadata on every field.</li>
 *   <li>Asserting the {@code ProponerAccionResponse} DTO is built
 *       by the mapper, not inline in the tool.</li>
 * </ul>
 *
 * <p>Test layer: pure Mockito unit — no Spring context, no
 * {@code ChatClient}, no OpenAI HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class ProponerAccionToolTest {

    @Mock
    private ProponerAccionUseCase proponerAccionUseCase;

    private ProponerAccionTool tool;

    // ── Happy path: staging a proposal ─────────────────────────────

    @BeforeEach
    void setUp() {
        tool = new ProponerAccionTool(proponerAccionUseCase);
    }

    @Test
    @DisplayName("proponerAccion calls ProponerAccionUseCase with the model-supplied request shape")
    void proponerAccion_stagesProposalViaUseCase() {
        com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse stub =
            new com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse(
                "accion-1", "PENDING"
            );
        when(proponerAccionUseCase.proponer(org.mockito.ArgumentMatchers.any()))
            .thenReturn(stub);

        ProponerAccionRequest request = new ProponerAccionRequest(
            "CREATE_TAREA",
            "{\"titulo\":\"Llamar al cliente\"}",
            "el cliente pidió seguimiento",
            1440
        );

        com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse result =
            tool.proponerAccion(request);

        assertNotNull(result);
        assertEquals("accion-1", result.accionId(),
            "result must surface the staged AiAccion id so the model can reference it");
        assertEquals("PENDING", result.estado(),
            "result must surface PENDING so the model confirms staging without mutation");

        ArgumentCaptor<ProponerAccionCommand> captor =
            ArgumentCaptor.forClass(ProponerAccionCommand.class);
        verify(proponerAccionUseCase).proponer(captor.capture());
        ProponerAccionCommand cmd = captor.getValue();

        // The tool MUST NOT enrich the command with actor/tenant/conv
        // scope — that is the use case's job (resolved through the
        // trusted AiToolContextPort).
        assertEquals(TipoAccion.CREATE_TAREA, cmd.tipo());
        assertEquals("{\"titulo\":\"Llamar al cliente\"}", cmd.payloadJson());
        assertEquals("el cliente pidió seguimiento", cmd.rationale());
        assertEquals(1440, cmd.ttlMinutos());
    }

    // ── Tool request DTO contract (@Tool + @ToolParam) ─────────────

    @Test
    @DisplayName("Request DTO is a separate class in dto/ — never nested in the tool class")
    void requestDto_isSeparateClassNotNested() {
        Class<?>[] declaredClasses = ProponerAccionTool.class.getDeclaredClasses();
        for (Class<?> c : declaredClasses) {
            assertFalse(c.getSimpleName().equals("Request"),
                "ProponerAccionTool.Request must be extracted to "
                    + "com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionRequest; "
                    + "nested DTOs are not allowed.");
            assertFalse(c.getSimpleName().equals("Result"),
                "ProponerAccionTool.Result must be extracted to "
                    + "com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse; "
                    + "nested DTOs are not allowed.");
        }
    }

    @Test
    @DisplayName("Request DTO is a record with @ToolParam on every component")
    void requestRecord_carriesToolParamMetadata() throws NoSuchMethodException {
        Class<?> requestClass = ProponerAccionRequest.class;
        Constructor<?> ctor = requestClass.getDeclaredConstructors()[0];
        Parameter[] params = ctor.getParameters();
        assertEquals(4, params.length,
            "ProponerAccionRequest must expose exactly 4 components: "
                + "tipoAccion, payloadJson, rationale, ttlMinutos");

        for (Parameter param : params) {
            ToolParam ann = param.getAnnotation(ToolParam.class);
            assertNotNull(ann,
                "every component of Request must be annotated with @ToolParam "
                    + "so Spring AI can publish field metadata to the model; "
                    + "missing annotation on: " + param.getName());
            assertFalse(ann.description().isBlank(),
                "@ToolParam on '" + param.getName() + "' must carry a non-blank "
                    + "description — the model uses it to choose the right value");
        }

        // Spot-check parameter order.
        assertEquals("tipoAccion", params[0].getName());
        assertEquals("payloadJson", params[1].getName());
        assertEquals("rationale", params[2].getName());
        assertEquals("ttlMinutos", params[3].getName());
    }

    // ── Constructor guard: tool MUST NOT depend on real mutation use cases ──

    @Test
    @DisplayName("Constructor declares ProponerAccionUseCase only — no real mutation use cases")
    void constructor_doesNotInjectRealMutationUseCases() {
        Constructor<?>[] ctors = ProponerAccionTool.class.getDeclaredConstructors();
        assertEquals(1, ctors.length,
            "ProponerAccionTool must expose exactly one constructor (Lombok @RequiredArgsConstructor)");

        Class<?>[] paramTypes = ctors[0].getParameterTypes();
        List<String> typeNames = new ArrayList<>();
        for (Class<?> t : paramTypes) {
            typeNames.add(t.getName());
        }

        // The tool depends on exactly ONE inbound use case. Real CRM
        // mutation use cases must NEVER appear here, otherwise the
        // safety boundary collapses.
        assertTrue(typeNames.contains(ProponerAccionUseCase.class.getName()),
            "tool must depend on the ProponerAccionUseCase; got: " + typeNames);

        assertFalse(typeNames.contains(CreateContactoUseCase.class.getName()),
            "tool MUST NOT depend on CreateContactoUseCase — that bypasses the "
                + "proposal lifecycle and turns the tool into a real mutation entry point");
        assertFalse(typeNames.contains(CreateTratoUseCase.class.getName()),
            "tool MUST NOT depend on CreateTratoUseCase — same safety boundary");
        assertFalse(typeNames.contains(CreateTareaUseCase.class.getName()),
            "tool MUST NOT depend on CreateTareaUseCase — same safety boundary");
        assertFalse(typeNames.contains(MoverColumnaFichaUseCase.class.getName()),
            "tool MUST NOT depend on MoverColumnaFichaUseCase — same safety boundary");
        // The tool must NOT directly depend on the staging use case
        // either — that would re-introduce the multi-dependency
        // coordination the user review asked to remove.
        assertFalse(typeNames.contains(RegistrarAccionUseCase.class.getName()),
            "tool MUST NOT depend on RegistrarAccionUseCase directly — the "
                + "staging use case is hidden behind ProponerAccionUseCase");
        assertEquals(1, paramTypes.length,
            "tool constructor must take exactly 1 parameter (ProponerAccionUseCase)");
    }

    @Test
    @DisplayName("Declared fields cover the constructor parameters — no surprise field injections")
    void declaredFields_matchConstructorParameters() {
        Field[] fields = ProponerAccionTool.class.getDeclaredFields();
        for (Field f : fields) {
            assertFalse(f.getType().equals(CreateContactoUseCase.class),
                "field '" + f.getName() + "' leaks CreateContactoUseCase into the tool");
            assertFalse(f.getType().equals(CreateTratoUseCase.class),
                "field '" + f.getName() + "' leaks CreateTratoUseCase into the tool");
            assertFalse(f.getType().equals(CreateTareaUseCase.class),
                "field '" + f.getName() + "' leaks CreateTareaUseCase into the tool");
            assertFalse(f.getType().equals(MoverColumnaFichaUseCase.class),
                "field '" + f.getName() + "' leaks MoverColumnaFichaUseCase into the tool");
        }
    }

    @Test
    @DisplayName("@Tool method is annotated so Spring AI can register it for the model")
    void toolMethod_isAnnotated() throws NoSuchMethodException {
        Method m = ProponerAccionTool.class.getDeclaredMethod(
            "proponerAccion", ProponerAccionRequest.class);
        Tool toolAnn = m.getAnnotation(Tool.class);
        assertNotNull(toolAnn,
            "proponerAccion(ProponerAccionRequest) must be annotated with @Tool so Spring AI "
                + "can register it as a callable tool for the model");
        assertFalse(toolAnn.description().isBlank(),
            "@Tool must carry a non-blank description — the model uses it to "
                + "decide when to invoke the tool");
        assertEquals("proponerAccion", toolAnn.name(),
            "tool name must be deterministic and intention-revealing for the model");
    }

    // ── Mapper contract: tool delegates command/response construction ──

    @Test
    @DisplayName("Tool class does not declare ProponerAccionCommand or DTO construction types as fields")
    void toolClass_doesNotHoldCommandOrDtoConstructionFields() {
        // The tool must delegate all command / DTO assembly to
        // ProponerAccionToolMapper. Holding these as fields would mean
        // the tool is re-creating the wire shapes inline, which the
        // adapter style forbids.
        Field[] fields = ProponerAccionTool.class.getDeclaredFields();
        for (Field f : fields) {
            assertFalse(f.getType().equals(ProponerAccionCommand.class),
                "tool must not hold ProponerAccionCommand as a field; "
                    + "commands are built per-call by the mapper");
            assertFalse(f.getType().equals(ProponerAccionRequest.class),
                "tool must not hold the request DTO as a field; DTOs are "
                    + "supplied per-call by the model");
            assertFalse(f.getType().equals(
                    com.ar.crm2.adapter.in.tool.ai.dto.ProponerAccionResponse.class),
                "tool must not hold the response DTO as a field; responses "
                    + "are built per-call by the mapper");
        }
    }
}
