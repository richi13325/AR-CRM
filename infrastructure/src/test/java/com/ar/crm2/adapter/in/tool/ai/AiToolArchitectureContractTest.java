package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.application.ai.port.in.ProponerAccionUseCase;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.ColumnaLecturaPort;
import com.ar.crm2.application.ai.port.out.ContactoLecturaPort;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.WhatsappMensajeLecturaPort;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.contacto.port.in.DeleteContactoUseCase;
import com.ar.crm2.application.contacto.port.in.EditContactoUseCase;
import com.ar.crm2.application.ficha.port.in.CreateFichaUseCase;
import com.ar.crm2.application.ficha.port.in.DeleteFichaUseCase;
import com.ar.crm2.application.ficha.port.in.EditFichaUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.tarea.port.in.DeleteTareaUseCase;
import com.ar.crm2.application.tarea.port.in.EditTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.application.trato.port.in.DeleteTratoUseCase;
import com.ar.crm2.application.trato.port.in.EditTratoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Architecture-contract test that pins the AI tool surface boundary
 * (Slice 14 — read-tools ports-vs-usecase decision/alignment).
 *
 * <p>Why this test exists. The full SDD verify report flagged that
 * four AI read tools call application read ports directly instead of
 * inbound use cases. The orchestrator directive resolved this as an
 * <b>explicit, coherent architecture decision</b>: AI read tools are
 * infrastructure input adapters that depend on <b>AI-bounded-context
 * outbound read ports</b> ({@link ContactoLecturaPort},
 * {@link ColumnaLecturaPort}, {@link WhatsappMensajeLecturaPort},
 * {@link FindAiResumenPort}) plus the trusted
 * {@link AiToolContextPort}. The propose tool
 * ({@link ProponerAccionTool}) is the only tool that calls an
 * inbound use case ({@link ProponerAccionUseCase}) because staging
 * has business logic to coordinate.
 *
 * <p>This test pins that contract so a future refactor cannot
 * accidentally inject a real CRM mutation use case into any tool
 * or convert a read port into a use case (or vice versa) without
 * the change being reviewed against this contract.
 *
 * <p>The contract has two halves:
 * <ol>
 *   <li><b>Read-tool contract</b> — the four read tools depend ONLY
 *       on {@code AiToolContextPort} + exactly one AI read port; the
 *       constructor MUST NOT inject any inbound use case
 *       (read or mutation), any other write port, or any other
 *       dependency.</li>
 *   <li><b>Propose-tool contract</b> — {@link ProponerAccionTool}
 *       depends ONLY on {@link ProponerAccionUseCase}; the
 *       constructor MUST NOT inject any read port, any real CRM
 *       mutation use case, or any other inbound use case.</li>
 * </ol>
 *
 * <p>The test is a pure reflection-based contract guard: no Spring
 * context, no Mockito, no live LLM. It is intentionally the
 * smallest possible boundary pin so a future reviewer can read the
 * contract in one file.
 */
@DisplayName("AI tool architecture contract — read tools use read ports, propose tool uses propose use case")
class AiToolArchitectureContractTest {

    /** Mutation / write use cases the AI tool surface MUST NEVER depend on. */
    private static final List<Class<?>> FORBIDDEN_MUTATION_USE_CASES = List.of(
        // contacto
        CreateContactoUseCase.class, EditContactoUseCase.class, DeleteContactoUseCase.class,
        // trato
        CreateTratoUseCase.class, EditTratoUseCase.class, DeleteTratoUseCase.class,
        // tarea
        CreateTareaUseCase.class, EditTareaUseCase.class, DeleteTareaUseCase.class,
        // ficha (Kanban move is the AI-propose-only mutation)
        CreateFichaUseCase.class, EditFichaUseCase.class, DeleteFichaUseCase.class,
        MoverColumnaFichaUseCase.class
    );

    /** Inbound use cases that no read tool is allowed to depend on. */
    private static final List<Class<?>> FORBIDDEN_INBOUND_USE_CASES_FOR_READ_TOOLS = List.of(
        // AI use cases — read tools have no business logic to wrap
        ProponerAccionUseCase.class,
        com.ar.crm2.application.ai.port.in.ConfirmarAccionUseCase.class,
        com.ar.crm2.application.ai.port.in.RechazarAccionUseCase.class,
        com.ar.crm2.application.ai.port.in.AnalizarChatUseCase.class,
        com.ar.crm2.application.ai.port.in.RegistrarAccionUseCase.class,
        com.ar.crm2.application.ai.port.in.RegistrarMensajeAsistenteUseCase.class,
        com.ar.crm2.application.ai.port.in.ObtenerAccionUseCase.class,
        com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase.class,
        com.ar.crm2.application.ai.port.in.ListarAccionesPendientesUseCase.class,
        com.ar.crm2.application.ai.port.in.ListarConversacionesAsistenteUseCase.class,
        com.ar.crm2.application.ai.port.in.ExpirarAccionUseCase.class,
        // contacto / trato / tarea / ficha CRUD use cases are also inbound use cases
        CreateContactoUseCase.class, EditContactoUseCase.class, DeleteContactoUseCase.class,
        CreateTratoUseCase.class, EditTratoUseCase.class, DeleteTratoUseCase.class,
        CreateTareaUseCase.class, EditTareaUseCase.class, DeleteTareaUseCase.class,
        CreateFichaUseCase.class, EditFichaUseCase.class, DeleteFichaUseCase.class,
        MoverColumnaFichaUseCase.class
    );

    /**
     * Read-port types the four read tools are allowed to depend on
     * (in addition to {@link AiToolContextPort}). The set is
     * intentionally explicit so a future "I just need one more
     * read port here" change is forced to update this test.
     */
    private static final Set<Class<?>> ALLOWED_READ_PORTS = Set.of(
        ContactoLecturaPort.class,
        ColumnaLecturaPort.class,
        WhatsappMensajeLecturaPort.class,
        FindAiResumenPort.class,
        // AI-internal write / read ports the propose tool orchestrates internally —
        // listed here ONLY for the propose-tool contract, NOT for read tools.
        com.ar.crm2.application.ai.port.out.SaveAiAccionPort.class,
        com.ar.crm2.application.ai.port.out.SaveAiMemoriaPort.class,
        com.ar.crm2.application.ai.port.out.DeleteAiMemoriaPort.class,
        com.ar.crm2.application.ai.port.out.SaveAiMensajePort.class,
        com.ar.crm2.application.ai.port.out.SaveAiResumenPort.class,
        com.ar.crm2.application.ai.port.out.SaveAiConversacionPort.class,
        com.ar.crm2.application.ai.port.out.FindAiAccionPort.class,
        com.ar.crm2.application.ai.port.out.FindAiConversacionPort.class,
        com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort.class,
        com.ar.crm2.application.ai.port.out.FindAiMemoriaPort.class,
        com.ar.crm2.application.ai.port.out.ListAiConversacionesPort.class,
        com.ar.crm2.application.ai.port.out.ListPendingAiAccionesPort.class,
        com.ar.crm2.application.ai.port.out.UpdateEstadoAccionPort.class,
        com.ar.crm2.application.ai.port.out.FichaLecturaPort.class,
        com.ar.crm2.application.ai.port.out.WhatsappConversacionLecturaPort.class,
        com.ar.crm2.application.ai.port.out.GenerarChatAsistentePort.class,
        com.ar.crm2.application.ai.port.out.GenerarEmbeddingPort.class,
        com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort.class
    );

    // ── Read-tool contract ─────────────────────────────────────────

    @Test
    @DisplayName("BuscarClientePorTelefonoTool: AiToolContextPort + ContactoLecturaPort, no inbound use case, no mutation use case")
    void buscarClientePorTelefonoTool_readOnlyContract() {
        Class<?>[] paramTypes = assertSingleConstructorAndGetParams(
            BuscarClientePorTelefonoTool.class,
            "BuscarClientePorTelefonoTool"
        );
        assertReadToolConstructorShape(
            BuscarClientePorTelefonoTool.class,
            paramTypes,
            ContactoLecturaPort.class
        );
        assertNoFieldLeaks(BuscarClientePorTelefonoTool.class, FORBIDDEN_MUTATION_USE_CASES,
            "BuscarClientePorTelefonoTool");
    }

    @Test
    @DisplayName("ListarColumnasTableroTool: ColumnaLecturaPort only (no AiToolContextPort needed — no trusted conv id is read), no use case")
    void listarColumnasTableroTool_readOnlyContract() {
        Class<?>[] paramTypes = assertSingleConstructorAndGetParams(
            ListarColumnasTableroTool.class,
            "ListarColumnasTableroTool"
        );
        // The board-type catalog is global (not tenant-scoped), so the tool
        // does not need AiToolContextPort. It depends ONLY on the read port.
        assertEquals(1, paramTypes.length,
            "ListarColumnasTableroTool must take exactly 1 collaborator: ColumnaLecturaPort");
        assertEquals(ColumnaLecturaPort.class, paramTypes[0],
            "ListarColumnasTableroTool must depend on ColumnaLecturaPort");
        assertNoFieldLeaks(ListarColumnasTableroTool.class, FORBIDDEN_INBOUND_USE_CASES_FOR_READ_TOOLS,
            "ListarColumnasTableroTool");
        assertNoFieldLeaks(ListarColumnasTableroTool.class, FORBIDDEN_MUTATION_USE_CASES,
            "ListarColumnasTableroTool");
    }

    @Test
    @DisplayName("ObtenerMensajesRecientesTool: AiToolContextPort + WhatsappMensajeLecturaPort, no inbound use case, no mutation use case")
    void obtenerMensajesRecientesTool_readOnlyContract() {
        Class<?>[] paramTypes = assertSingleConstructorAndGetParams(
            ObtenerMensajesRecientesTool.class,
            "ObtenerMensajesRecientesTool"
        );
        assertReadToolConstructorShape(
            ObtenerMensajesRecientesTool.class,
            paramTypes,
            WhatsappMensajeLecturaPort.class
        );
        assertNoFieldLeaks(ObtenerMensajesRecientesTool.class, FORBIDDEN_MUTATION_USE_CASES,
            "ObtenerMensajesRecientesTool");
    }

    @Test
    @DisplayName("ObtenerResumenChatTool: AiToolContextPort + FindAiResumenPort, no inbound use case, no mutation use case")
    void obtenerResumenChatTool_readOnlyContract() {
        Class<?>[] paramTypes = assertSingleConstructorAndGetParams(
            ObtenerResumenChatTool.class,
            "ObtenerResumenChatTool"
        );
        assertReadToolConstructorShape(
            ObtenerResumenChatTool.class,
            paramTypes,
            FindAiResumenPort.class
        );
        assertNoFieldLeaks(ObtenerResumenChatTool.class, FORBIDDEN_MUTATION_USE_CASES,
            "ObtenerResumenChatTool");
    }

    // ── Propose-tool contract ──────────────────────────────────────

    @Test
    @DisplayName("ProponerAccionTool: ONLY ProponerAccionUseCase, no read port, no real mutation use case, no other inbound use case")
    void proponerAccionTool_proposeOnlyContract() {
        Class<?>[] paramTypes = assertSingleConstructorAndGetParams(
            ProponerAccionTool.class,
            "ProponerAccionTool"
        );
        // Propose tool MUST depend on exactly the staging use case — no
        // read port, no real CRM mutation use case, no other inbound use
        // case, no save port (those are coordination the use case owns).
        assertEquals(1, paramTypes.length,
            "ProponerAccionTool must take exactly 1 collaborator: ProponerAccionUseCase");
        assertEquals(ProponerAccionUseCase.class, paramTypes[0],
            "ProponerAccionTool must depend on ProponerAccionUseCase (the only inbound use case in the tool surface)");
        // Defence in depth — the propose tool MUST NOT have any mutation
        // use case as a field (e.g. injected via a future Lombok @RequiredArgsConstructor expansion).
        for (Class<?> forbidden : FORBIDDEN_MUTATION_USE_CASES) {
            assertNoFieldType(ProponerAccionTool.class, forbidden,
                "ProponerAccionTool field leaks " + forbidden.getSimpleName());
        }
        // Defence in depth — the propose tool MUST NOT have any read port
        // as a field (read access is owned by the use case's internal
        // service, never by the tool itself).
        for (Class<?> readPort : Set.of(
                ContactoLecturaPort.class,
                ColumnaLecturaPort.class,
                WhatsappMensajeLecturaPort.class,
                FindAiResumenPort.class,
                com.ar.crm2.application.ai.port.out.FichaLecturaPort.class)) {
            assertNoFieldType(ProponerAccionTool.class, readPort,
                "ProponerAccionTool field leaks " + readPort.getSimpleName());
        }
    }

    // ── Cross-tool invariants ──────────────────────────────────────

    @Test
    @DisplayName("All 5 tools: each tool exposes exactly one @Tool method and that method is annotated")
    void everyTool_declaresExactlyOneToolMethod() throws NoSuchMethodException {
        List<Class<?>> toolClasses = List.of(
            BuscarClientePorTelefonoTool.class,
            ListarColumnasTableroTool.class,
            ObtenerMensajesRecientesTool.class,
            ObtenerResumenChatTool.class,
            ProponerAccionTool.class
        );
        for (Class<?> toolClass : toolClasses) {
            int toolCount = 0;
            for (Method m : toolClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Tool.class)) {
                    toolCount++;
                    assertNotNull(m.getAnnotation(Tool.class).name(),
                        "Tool method on " + toolClass.getSimpleName() + " must declare a deterministic name");
                    assertFalse(m.getAnnotation(Tool.class).description().isBlank(),
                        "Tool method on " + toolClass.getSimpleName() + " must declare a non-blank description");
                }
            }
            assertEquals(1, toolCount,
                toolClass.getSimpleName() + " must expose exactly one @Tool method");
        }
    }

    @Test
    @DisplayName("Read tools' @Tool descriptions declare the read-only invariant verbatim")
    void readTools_describeReadOnlyInvariant() throws NoSuchMethodException {
        // The model relies on the @Tool description to decide whether
        // a tool mutates. Each read tool MUST declare "Read-only"
        // so the model never proposes the tool as a mutation entry point.
        assertDescriptionContains(BuscarClientePorTelefonoTool.class, "Read-only");
        assertDescriptionContains(ListarColumnasTableroTool.class, "Read-only");
        assertDescriptionContains(ObtenerMensajesRecientesTool.class, "Read-only");
        assertDescriptionContains(ObtenerResumenChatTool.class, "Read-only");
    }

    @Test
    @DisplayName("Propose tool's @Tool description declares the propose-only invariant (PENDING + human confirmation)")
    void proponerAccionTool_describeProposeOnlyInvariant() throws NoSuchMethodException {
        assertDescriptionContains(ProponerAccionTool.class, "PENDING");
        assertDescriptionContains(ProponerAccionTool.class, "confirm");
    }

    // ── Test helpers ───────────────────────────────────────────────

    /**
     * Asserts the class exposes exactly one constructor (Lombok
     * {@code @RequiredArgsConstructor}) and returns its parameter
     * types in declaration order.
     */
    private static Class<?>[] assertSingleConstructorAndGetParams(
            Class<?> toolClass, String label) {
        Constructor<?>[] ctors = toolClass.getDeclaredConstructors();
        assertEquals(1, ctors.length,
            label + " must expose exactly one constructor (Lombok @RequiredArgsConstructor)");
        return ctors[0].getParameterTypes();
    }

    /**
     * Asserts a read-tool constructor shape:
     * <ul>
     *   <li>parameter count: 1 or 2</li>
     *   <li>first parameter (if 2): {@link AiToolContextPort}</li>
     *   <li>remaining parameter: the expected AI read port</li>
     *   <li>NO inbound use case in the parameter list</li>
     *   <li>NO mutation use case in the parameter list</li>
     * </ul>
     */
    private static void assertReadToolConstructorShape(
            Class<?> toolClass, Class<?>[] paramTypes, Class<?> expectedReadPort) {
        List<String> typeNames = new ArrayList<>();
        for (Class<?> t : paramTypes) {
            typeNames.add(t.getName());
        }
        assertTrue(paramTypes.length == 1 || paramTypes.length == 2,
            toolClass.getSimpleName() + " must take 1 or 2 collaborators (trusted context + read port). Got: " + typeNames);
        // First parameter — if two collaborators — must be the trusted context port.
        if (paramTypes.length == 2) {
            assertEquals(AiToolContextPort.class, paramTypes[0],
                toolClass.getSimpleName() + " first collaborator must be AiToolContextPort (trusted scope). Got: " + typeNames);
        }
        // The non-context parameter (last) must be the expected read port.
        Class<?> readPortParam = paramTypes[paramTypes.length - 1];
        assertEquals(expectedReadPort, readPortParam,
            toolClass.getSimpleName() + " must depend on " + expectedReadPort.getSimpleName() + ". Got: " + typeNames);
        // No inbound use case of any kind in the parameter list.
        for (Class<?> forbidden : FORBIDDEN_INBOUND_USE_CASES_FOR_READ_TOOLS) {
            assertFalse(typeNames.contains(forbidden.getName()),
                toolClass.getSimpleName() + " constructor must NOT inject the inbound use case "
                    + forbidden.getSimpleName() + " — read tools depend on AI read ports, not use cases");
        }
        // No mutation use case in the parameter list.
        for (Class<?> forbidden : FORBIDDEN_MUTATION_USE_CASES) {
            assertFalse(typeNames.contains(forbidden.getName()),
                toolClass.getSimpleName() + " constructor must NOT inject the mutation use case "
                    + forbidden.getSimpleName());
        }
        // No save / update / delete AI port in the parameter list (read tools
        // are read-only by design; no save/update/delete port may leak in).
        for (Class<?> writePort : List.of(
                com.ar.crm2.application.ai.port.out.SaveAiAccionPort.class,
                com.ar.crm2.application.ai.port.out.SaveAiMemoriaPort.class,
                com.ar.crm2.application.ai.port.out.DeleteAiMemoriaPort.class,
                com.ar.crm2.application.ai.port.out.SaveAiMensajePort.class,
                com.ar.crm2.application.ai.port.out.SaveAiResumenPort.class,
                com.ar.crm2.application.ai.port.out.SaveAiConversacionPort.class,
                com.ar.crm2.application.ai.port.out.UpdateEstadoAccionPort.class)) {
            assertFalse(typeNames.contains(writePort.getName()),
                toolClass.getSimpleName() + " constructor must NOT inject the write port "
                    + writePort.getSimpleName() + " — read tools are read-only");
        }
    }

    /**
     * Asserts no declared field on the tool has any of the forbidden
     * types. Defends against future edits that inject a mutation
     * use case via a Lombok @RequiredArgsConstructor expansion or a
     * manual @Autowired field.
     */
    private static void assertNoFieldLeaks(Class<?> toolClass, List<Class<?>> forbiddenTypes, String label) {
        for (Class<?> forbidden : forbiddenTypes) {
            assertNoFieldType(toolClass, forbidden,
                label + " field leaks " + forbidden.getSimpleName());
        }
    }

    private static void assertNoFieldType(Class<?> toolClass, Class<?> forbidden, String message) {
        for (Field f : toolClass.getDeclaredFields()) {
            assertFalse(f.getType().equals(forbidden),
                message + " — declared on field '" + f.getName() + "'");
        }
    }

    private static void assertDescriptionContains(Class<?> toolClass, String fragment) throws NoSuchMethodException {
        for (Method m : toolClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Tool.class)) {
                String description = m.getAnnotation(Tool.class).description();
                assertTrue(description.contains(fragment),
                    toolClass.getSimpleName() + "." + m.getName()
                        + " @Tool description must contain '" + fragment + "' so the model knows the tool's invariant. "
                        + "Actual description: \"" + description + "\"");
                return;
            }
        }
        throw new AssertionError(toolClass.getSimpleName() + " has no @Tool-annotated method");
    }
}
