package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ProponerAccionCommand;
import com.ar.crm2.application.ai.command.RegistrarAccionCommand;
import com.ar.crm2.application.ai.port.in.ProponerAccionUseCase;
import com.ar.crm2.application.ai.port.in.RegistrarAccionUseCase;
import com.ar.crm2.application.ai.port.in.result.ProponerAccionResponse;
import com.ar.crm2.application.ai.port.out.AiToolContextPort;
import com.ar.crm2.application.ai.port.out.dto.AiToolContext;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.enums.TipoAccion;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RED-first tests for {@link ProponerAccionService}.
 *
 * <p>This service is the inbound use case called by the
 * {@code ProponerAccionTool} Spring AI input adapter. It coordinates
 * the trusted scope resolution and the staging of the proposal so the
 * tool adapter only needs ONE inbound collaborator (the use case) and
 * never coordinates the resolver + staging use case inline.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Happy path: resolves trusted context, builds a
 *       {@code RegistrarAccionCommand}, calls the staging use case,
 *       returns a {@code ProponerAccionResponse} with the staged id
 *       and estado.</li>
 *   <li>Missing context from the resolver propagates and the staging
 *       use case is never called (no unscoped staging).</li>
 *   <li>The service does NOT depend on real mutation use cases
 *       (CreateContacto / CreateTrato / CreateTarea / MoverColumnaFicha)
 *       — the safety boundary is enforced by the inbound use case
 *       contract.</li>
 *   <li>Safety boundary: real mutation use cases must NEVER appear as
 *       constructor parameters (reflection guard).</li>
 * </ul>
 */
class ProponerAccionServiceTest {

    private static final UUID ACTOR = UUID.fromString("aaaaaaaa-1111-2222-3333-444444444444");
    private static final UUID EMPRESA = UUID.fromString("bbbbbbbb-1111-2222-3333-444444444444");
    private static final UUID AI_CONV = UUID.fromString("cccccccc-1111-2222-3333-444444444444");

    private InMemoryContextResolver contextResolver;
    private InMemoryRegistrarAccionUseCase registrarAccion;
    private ProponerAccionService service;

    @BeforeEach
    void setUp() {
        contextResolver = new InMemoryContextResolver();
        contextResolver.next = new AiToolContext(ACTOR, EMPRESA, AI_CONV, "wa-conv-99");
        registrarAccion = new InMemoryRegistrarAccionUseCase();
        service = new ProponerAccionService(contextResolver, registrarAccion);
    }

    @Test
    @DisplayName("proponer stages a PENDING proposal via the staging use case and returns the staged id")
    void proponer_stagesPendingProposalAndReturnsId() {
        AiAccion persisted = AiAccion.reconstitute(
            AiAccionId.from(UUID.randomUUID()),
            EmpresaId.from(EMPRESA),
            UsuarioId.from(ACTOR),
            "wa-conv-99",
            null,
            AiConversacionId.from(AI_CONV),
            "CREATE_TAREA",
            "{\"titulo\":\"Llamar\"}",
            "r",
            1,
            LocalDateTime.now().plusMinutes(60),
            null, null,
            EstadoAccion.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        registrarAccion.next = persisted;

        ProponerAccionCommand command = new ProponerAccionCommand(
            TipoAccion.CREATE_TAREA,
            "{\"titulo\":\"Llamar\"}",
            "r",
            60
        );

        ProponerAccionResponse result = service.proponer(command);

        assertNotNull(result);
        assertEquals(persisted.getId().value().toString(), result.accionId(),
            "the use case must return the staged AiAccion id so the tool "
                + "can surface it to the model");
        assertEquals("PENDING", result.estado(),
            "the use case must surface PENDING — staging is not mutation");
        assertEquals(1, registrarAccion.received.size());
        RegistrarAccionCommand staged = registrarAccion.received.get(0);
        assertEquals(ACTOR, staged.actorUsuarioId(),
            "actor must come from the trusted resolver, never from the model payload");
        assertEquals(EMPRESA, staged.empresaId(),
            "tenant must come from the trusted resolver — the model cannot spoof it");
        assertEquals(AI_CONV, staged.aiConversacionId(),
            "aiConversacionId must come from the trusted resolver for audit linkage");
        assertEquals("wa-conv-99", staged.waConversacionId());
        assertEquals("CREATE_TAREA", staged.tipoAccion());
        assertEquals("{\"titulo\":\"Llamar\"}", staged.payloadJson());
        assertEquals("r", staged.rationale());
        assertEquals(60, staged.ttlMinutos());
    }

    @Test
    @DisplayName("proponer rejects when the resolver throws (no unscoped staging)")
    void proposer_resolverFails_doesNotStage() {
        contextResolver.next = null;
        contextResolver.throwOnResolve = new IllegalStateException(
            "no ai context in current thread"
        );

        ProponerAccionCommand command = new ProponerAccionCommand(
            TipoAccion.CREATE_CONTACTO, "{\"nombre\":\"x\"}", "r", 60
        );

        assertThrows(IllegalStateException.class, () -> service.proponer(command));
        assertEquals(0, registrarAccion.received.size(),
            "the staging use case must not be called when the resolver fails");
    }

    @Test
    @DisplayName("proponer propagates IllegalStateException from the staging use case unchanged")
    void proponer_stagingFailurePropagates() {
        registrarAccion.throwOnStage = new IllegalStateException("upstream down");

        ProponerAccionCommand command = new ProponerAccionCommand(
            TipoAccion.CREATE_TRATO, "{\"nombre\":\"x\"}", "r", 60
        );

        assertThrows(IllegalStateException.class, () -> service.proponer(command));
    }

    // ── Safety boundary: real mutation use cases must NEVER appear ──

    @Test
    @DisplayName("Constructor depends only on AiToolContextPort + RegistrarAccionUseCase — no real mutation use cases")
    void constructor_doesNotInjectRealMutationUseCases() {
        Constructor<?>[] ctors = ProponerAccionService.class.getDeclaredConstructors();
        assertEquals(1, ctors.length,
            "ProponerAccionService must expose exactly one constructor (Lombok @RequiredArgsConstructor)");

        Class<?>[] paramTypes = ctors[0].getParameterTypes();
        List<String> typeNames = new ArrayList<>();
        for (Class<?> t : paramTypes) {
            typeNames.add(t.getName());
        }

        assertTrue(typeNames.contains(AiToolContextPort.class.getName()),
            "service must depend on the context port; got: " + typeNames);
        assertTrue(typeNames.contains(RegistrarAccionUseCase.class.getName()),
            "service must depend on the staging use case; got: " + typeNames);

        for (String forbidden : List.of(
            "com.ar.crm2.application.contacto.port.in.CreateContactoUseCase",
            "com.ar.crm2.application.trato.port.in.CreateTratoUseCase",
            "com.ar.crm2.application.tarea.port.in.CreateTareaUseCase",
            "com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase"
        )) {
            assertTrue(!typeNames.contains(forbidden),
                "ProponerAccionService MUST NOT depend on " + forbidden
                    + " — that would bypass the proposal lifecycle");
        }
        assertEquals(2, paramTypes.length,
            "service constructor must take exactly 2 parameters "
                + "(AiToolContextPort, RegistrarAccionUseCase)");
    }

    // ── In-memory collaborators ──

    private static final class InMemoryContextResolver implements AiToolContextPort {
        AiToolContext next;
        RuntimeException throwOnResolve;

        @Override
        public AiToolContext resolve() {
            if (throwOnResolve != null) {
                throw throwOnResolve;
            }
            return next;
        }
    }

    private static final class InMemoryRegistrarAccionUseCase implements RegistrarAccionUseCase {
        final List<RegistrarAccionCommand> received = new ArrayList<>();
        AiAccion next;
        RuntimeException throwOnStage;

        @Override
        public AiAccion registrar(RegistrarAccionCommand command) {
            if (throwOnStage != null) {
                throw throwOnStage;
            }
            received.add(command);
            return next != null ? next : AiAccion.reconstitute(
                AiAccionId.from(UUID.randomUUID()),
                EmpresaId.from(command.empresaId()),
                UsuarioId.from(command.actorUsuarioId()),
                command.waConversacionId(),
                null,
                AiConversacionId.from(command.aiConversacionId()),
                command.tipoAccion(),
                command.payloadJson(),
                command.rationale(),
                command.ttlMinutos(),
                LocalDateTime.now().plusMinutes(command.ttlMinutos()),
                null, null,
                EstadoAccion.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        }
    }

    @SuppressWarnings("unused") // type-level guard — these references enforce the safety boundary
    private static final class GuardedMutationUseCases {
        private com.ar.crm2.application.contacto.port.in.CreateContactoUseCase createContacto;
        private com.ar.crm2.application.trato.port.in.CreateTratoUseCase createTrato;
        private com.ar.crm2.application.tarea.port.in.CreateTareaUseCase createTarea;
        private com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase moverColumnaFicha;
    }
}
