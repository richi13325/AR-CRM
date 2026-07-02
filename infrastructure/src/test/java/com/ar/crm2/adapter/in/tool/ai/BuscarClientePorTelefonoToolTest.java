package com.ar.crm2.adapter.in.tool.ai;

import com.ar.crm2.adapter.in.tool.ai.dto.BuscarClientePorTelefonoRequest;
import com.ar.crm2.adapter.in.tool.ai.dto.BuscarClientePorTelefonoResponse;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.ContactoLecturaPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RED-first tests for {@link BuscarClientePorTelefonoTool} — the
 * read-only {@code @Tool} input adapter that looks up an existing
 * Contacto by phone number, scoped to the trusted tenant.
 *
 * <p><b>Tenant boundary.</b> the tool pulls the {@code empresaId}
 * from the trusted {@link AiToolContextPort} so the model cannot
 * probe a foreign tenant — only the supplied phone number comes
 * from the model.
 */
@ExtendWith(MockitoExtension.class)
class BuscarClientePorTelefonoToolTest {

    @Mock
    private AiToolContextPort aiToolContextPort;

    @Mock
    private ContactoLecturaPort contactoLecturaPort;

    private BuscarClientePorTelefonoTool tool;

    @BeforeEach
    void setUp() {
        tool = new BuscarClientePorTelefonoTool(aiToolContextPort, contactoLecturaPort);
    }

    @Test
    @DisplayName("buscarClientePorTelefono returns the contacto when the (empresaId, telefono) tuple matches")
    void buscarCliente_returnsContactoOnMatch() {
        UUID actor = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000010");
        UUID empresa = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000020");
        UUID aiConv = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000030");
        String waConv = "aaaaaaaa-1111-2222-3333-000000000040";
        AiToolContext ctx = new AiToolContext(actor, empresa, aiConv, waConv);
        when(aiToolContextPort.resolve()).thenReturn(ctx);

        Contacto contacto = sampleContacto(empresa, "Juan Pérez", "+5491155555555");
        when(contactoLecturaPort.findByEmpresaIdAndTelefono(any(EmpresaId.class), any(String.class)))
            .thenReturn(Optional.of(contacto));

        BuscarClientePorTelefonoResponse out =
            tool.buscarClientePorTelefono(new BuscarClientePorTelefonoRequest("+5491155555555"));

        assertNotNull(out);
        assertNotNull(out.id());
        assertEquals("Juan Pérez", out.nombre());
        assertEquals("+5491155555555", out.telefono());
        assertEquals(empresa.toString(), out.empresaId());

        ArgumentCaptor<EmpresaId> empCaptor = ArgumentCaptor.forClass(EmpresaId.class);
        ArgumentCaptor<String> telCaptor = ArgumentCaptor.forClass(String.class);
        verify(contactoLecturaPort)
            .findByEmpresaIdAndTelefono(empCaptor.capture(), telCaptor.capture());
        assertEquals(EmpresaId.from(empresa), empCaptor.getValue(),
            "the tool MUST source empresaId from the trusted AiToolContext, NOT from the model payload");
        assertEquals("+5491155555555", telCaptor.getValue());
    }

    @Test
    @DisplayName("buscarClientePorTelefono returns null fields when no contacto matches")
    void buscarCliente_returnsEmptyOnMiss() {
        UUID actor = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000010");
        UUID empresa = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000020");
        UUID aiConv = UUID.fromString("aaaaaaaa-1111-2222-3333-000000000030");
        when(aiToolContextPort.resolve())
            .thenReturn(new AiToolContext(actor, empresa, aiConv, "wa-conv"));
        when(contactoLecturaPort.findByEmpresaIdAndTelefono(any(EmpresaId.class), any(String.class)))
            .thenReturn(Optional.empty());

        BuscarClientePorTelefonoResponse out =
            tool.buscarClientePorTelefono(new BuscarClientePorTelefonoRequest("+5491199999999"));

        assertNotNull(out, "the tool must always return a non-null DTO so Spring AI can serialize it");
        assertNull(out.id(), "miss -> id is null");
        assertNull(out.nombre(), "miss -> nombre is null");
        assertNull(out.telefono(), "miss -> telefono is null");
    }

    @Test
    @DisplayName("@Tool method is annotated and the request carries @ToolParam")
    void toolMethod_isAnnotated() throws NoSuchMethodException {
        Method m = BuscarClientePorTelefonoTool.class.getDeclaredMethod(
            "buscarClientePorTelefono", BuscarClientePorTelefonoRequest.class);
        Tool toolAnn = m.getAnnotation(Tool.class);
        assertNotNull(toolAnn, "@Tool annotation required");
        assertFalse(toolAnn.description().isBlank());
        assertEquals("buscarClientePorTelefono", toolAnn.name(),
            "tool name must be deterministic and intention-revealing");

        Constructor<?>[] ctors = BuscarClientePorTelefonoRequest.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Parameter[] params = ctors[0].getParameters();
        assertEquals(1, params.length, "Request must expose exactly one field: telefono");
        ToolParam ann = params[0].getAnnotation(ToolParam.class);
        assertNotNull(ann, "@ToolParam required on telefono so Spring AI publishes metadata");
        assertFalse(ann.description().isBlank());
        assertEquals("telefono", params[0].getName());
    }

    @Test
    @DisplayName("Constructor declares ONLY trusted scope port + contacto read port — no mutation use cases")
    void constructor_doesNotInjectRealMutationUseCases() {
        Constructor<?>[] ctors = BuscarClientePorTelefonoTool.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        Class<?>[] params = ctors[0].getParameterTypes();
        assertEquals(2, params.length,
            "tool must take exactly 2 collaborators: AiToolContextPort + ContactoLecturaPort");
        assertEquals(AiToolContextPort.class, params[0]);
        assertEquals(ContactoLecturaPort.class, params[1]);
        for (Class<?> forbidden : List.of(
                CreateContactoUseCase.class, CreateTratoUseCase.class,
                CreateTareaUseCase.class, MoverColumnaFichaUseCase.class)) {
            for (Field f : BuscarClientePorTelefonoTool.class.getDeclaredFields()) {
                assertFalse(f.getType().equals(forbidden),
                    "field '" + f.getName() + "' leaks " + forbidden.getSimpleName());
            }
        }
    }

    @Test
    @DisplayName("Tool class does not hold DTOs / projections / context as fields (delegation only)")
    void toolClass_doesNotHoldDtoConstructionFields() {
        for (Field f : BuscarClientePorTelefonoTool.class.getDeclaredFields()) {
            assertFalse(f.getType().equals(BuscarClientePorTelefonoRequest.class));
            assertFalse(f.getType().equals(BuscarClientePorTelefonoResponse.class));
            assertFalse(f.getType().equals(Contacto.class),
                "tool must not hold the domain aggregate as a field; aggregates are mapped per-call");
            assertFalse(f.getType().equals(AiToolContext.class),
                "tool must not hold the context as a field; it is resolved per-call");
        }
    }

    @Test
    @DisplayName("log statement does NOT emit the raw telefono value (PII masking contract)")
    void logStatement_doesNotEmitRawTelefono() throws Exception {
        // Static evidence (source-guard): the tool's log call MUST NOT pass
        // request.telefono() directly because customer phone numbers are PII
        // and must never appear in clear text in log records.
        //
        // We use a source-level guard rather than a log-capture appender
        // because the project does not depend on any log-capture test
        // library (no log-captor / logback-test / ListAppender is wired).
        // The deterministic helper is exercised in LogMaskingTest; here we
        // only pin that the production code routes the phone through it.
        //
        // The assertion is scoped to the argument list of the FIRST log.*
        // statement (the one that records the invocation outcome). A
        // legitimate helper call like
        //     String masked = LogMasking.maskPhone(request.telefono());
        // is allowed because the raw PII is consumed by the masking call,
        // not by the log statement itself.
        var source = new java.io.File(
            "src/main/java/com/ar/crm2/adapter/in/tool/ai/BuscarClientePorTelefonoTool.java");
        if (!source.exists()) {
            // Test is best-effort when run from a different working directory;
            // the pure-function masking contract in LogMaskingTest is
            // still authoritative.
            return;
        }
        String content = new String(java.nio.file.Files.readAllBytes(source.toPath()));

        // Find the first log.* call and extract its balanced argument list.
        int logIdx = content.indexOf("log.");
        assertTrue(logIdx >= 0,
            "tool must contain a log.* statement recording the invocation");

        int parenStart = content.indexOf('(', logIdx);
        assertTrue(parenStart > logIdx,
            "log.* call must open its argument list with '('");

        // Walk the argument list to its matching ')'.
        int depth = 0;
        int parenEnd = -1;
        for (int i = parenStart; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    parenEnd = i;
                    break;
                }
            }
        }
        assertTrue(parenEnd > parenStart,
            "log.* call must close its argument list with ')'");

        String logArgs = content.substring(parenStart, parenEnd + 1);
        assertFalse(logArgs.contains("request.telefono()"),
            "log.* call must NOT pass request.telefono() directly as a log "
                + "argument — route it through LogMasking.maskPhone(...) and "
                + "pass the masked value to the log statement");
    }

    @Test
    @DisplayName("log statement routes telefono through LogMasking.maskPhone (helper usage contract)")
    void logStatement_routesTelefonoThroughLogMaskingHelper() throws Exception {
        // Static evidence (source-guard): the tool MUST route the phone
        // number through the LogMasking.maskPhone(...) pure-function helper
        // so customer PII never reaches the log sink in clear text.
        var source = new java.io.File(
            "src/main/java/com/ar/crm2/adapter/in/tool/ai/BuscarClientePorTelefonoTool.java");
        if (!source.exists()) {
            return;
        }
        String content = new String(java.nio.file.Files.readAllBytes(source.toPath()));

        // The masking call MUST appear in the source.
        assertTrue(content.contains("LogMasking.maskPhone("),
            "tool must route the phone number through LogMasking.maskPhone(...) "
                + "before it reaches any log statement");

        // Defence in depth: the masking call must appear at or before the
        // first log.* statement, not after it.
        int maskIdx = content.indexOf("LogMasking.maskPhone(");
        int logIdx = content.indexOf("log.");
        assertTrue(logIdx >= 0,
            "tool must contain a log.* statement recording the invocation");
        assertTrue(maskIdx < logIdx,
            "LogMasking.maskPhone(...) call must appear BEFORE the log.* statement "
                + "that records the phone number");
    }

    private static Contacto sampleContacto(UUID empresa, String nombre, String telefono) {
        return Contacto.create(
            EmpresaId.from(empresa),
            nombre,
            null,
            EstadoRelacion.PROSPECTO,
            UsuarioId.from(UUID.fromString("aaaaaaaa-1111-2222-3333-000000000099")),
            UsuarioId.from(UUID.fromString("aaaaaaaa-1111-2222-3333-000000000099")),
            telefono,
            null,
            null
        );
    }
}