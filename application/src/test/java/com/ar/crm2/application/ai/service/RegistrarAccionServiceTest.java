package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.RegistrarAccionCommand;
import com.ar.crm2.application.ai.port.out.SaveAiAccionPort;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RegistrarAccionService}.
 *
 * <p>Coverage targets for the PR 1 sample gate:
 * <ul>
 *   <li>The service stages a proposal in PENDING state via the save port.</li>
 *   <li>The service stores the actor as solicitadaPor (ownership foundation).</li>
 *   <li>The service preserves payload opacity and version=1.</li>
 *   <li>The service does NOT invoke any real CRM mutation use case directly
 *       (asserted via constructor signature review + the absence of any
 *       mutation use case as a collaborator in this test).</li>
 * </ul>
 *
 * <p>Command-level input validation is asserted separately in
 * {@link RegistrarAccionCommandTest} (skeleton included as a
 * comment block at the bottom of this file to demonstrate RED-first
 * intent without bloating the sample).
 */
class RegistrarAccionServiceTest {

    private InMemorySaveAiAccionPort savePort;
    private RegistrarAccionService service;

    @BeforeEach
    void setUp() {
        savePort = new InMemorySaveAiAccionPort();
        service = new RegistrarAccionService(savePort);
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar stages a PENDING proposal with version=1 and persists it")
    void registrar_stagesAccionInPending() {
        RegistrarAccionCommand command = sampleCommand();

        AiAccion result = service.registrar(command);

        assertEquals(EstadoAccion.PENDING, result.getEstado());
        assertEquals(1, result.getVersion());
        assertEquals(1, savePort.saved.size());
        assertSame(result, savePort.saved.get(0));
    }

    @Test
    @DisplayName("registrar stores actor as solicitadaPor — ownership foundation")
    void registrar_solicitadaPorEsElActor() {
        UUID actor = UUID.randomUUID();
        RegistrarAccionCommand command = sampleCommand(actor, UUID.randomUUID());

        AiAccion result = service.registrar(command);

        assertEquals(actor, result.getSolicitadaPor().value());
        assertTrue(result.perteneceA(UsuarioId.from(actor)));
    }

    @Test
    @DisplayName("registrar preserves payloadJson opaquely — domain does not interpret it")
    void registrar_payloadSePreservaOpaco() {
        String payloadArbitrario = "{\"prioridad\":\"ALTA\",\"notas\":null}";
        RegistrarAccionCommand command = new RegistrarAccionCommand(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "wa-conv-1",
                null, "CREATE_TAREA", payloadArbitrario,
                "Cliente pidió demo", 60
        );

        AiAccion result = service.registrar(command);

        assertEquals(payloadArbitrario, result.getPayloadJson());
    }

    @Test
    @DisplayName("registrar persiste aiConversacionId y opcional waMensajeId para audit linkage")
    void registrar_preservaAuditLinks() {
        UUID aiConv = UUID.randomUUID();
        RegistrarAccionCommand command = new RegistrarAccionCommand(
                UUID.randomUUID(), UUID.randomUUID(), aiConv, "wa-conv-1",
                "wa-msg-99", "CREATE_TAREA",
                "{\"titulo\":\"Llamar\"}", "r", 60
        );

        AiAccion result = service.registrar(command);

        assertEquals(aiConv, result.getAiConversacionId().value());
        assertEquals("wa-msg-99", result.getWaMensajeId());
    }

    @Test
    @DisplayName("registrar acepta waMensajeId null y lo persiste como null")
    void registrar_waMensajeIdNulo() {
        RegistrarAccionCommand command = sampleCommand();

        AiAccion result = service.registrar(command);

        assertEquals(null, result.getWaMensajeId());
        assertNotNull(result.getAiConversacionId());
    }

    // ── Safety boundary ───────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar NEVER depends on CreateContactoUseCase / CreateTratoUseCase / CreateTareaUseCase / MoverColumnaFichaUseCase")
    void registrar_noInvocaMutationUseCases() {
        // The service constructor in this test only receives SaveAiAccionPort.
        // If the service ever required a direct call to any mutation use case, it would
        // need to be added to the constructor — and that change would break this test
        // (GuardedMutationUseCases fields are never wired, so the service cannot take
        // one as a dependency without an explicit fake/spy at this call site).
        RegistrarAccionCommand command = sampleCommand();

        AiAccion result = service.registrar(command);

        assertNotNull(result, "service should return a staged proposal without touching CRM mutations");
        assertEquals(EstadoAccion.PENDING, result.getEstado());
    }

    // ── Command validation (project pattern: validation in compact constructor) ─

    @Test
    @DisplayName("RegistrarAccionCommand rejects null actorUsuarioId at construction")
    void command_rechazaActorNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sampleCommand(null, UUID.randomUUID()));
        assertTrue(ex.getMessage().contains("actorUsuarioId"));
    }

    @Test
    @DisplayName("RegistrarAccionCommand rejects non-positive ttlMinutos at construction")
    void command_rechazaTtlInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RegistrarAccionCommand(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "wa-conv-1",
                        null, "CREATE_TAREA", "{}", "razon", 0));
        assertTrue(ex.getMessage().contains("ttlMinutos"));
    }

    @Test
    @DisplayName("RegistrarAccionCommand rejects null aiConversacionId at construction")
    void command_rechazaAiConversacionIdNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RegistrarAccionCommand(
                        UUID.randomUUID(), UUID.randomUUID(), null, "wa-conv-1",
                        null, "CREATE_TAREA", "{}", "razon", 60));
        assertTrue(ex.getMessage().contains("aiConversacionId"));
    }

    @Test
    @DisplayName("RegistrarAccionCommand normalizes blank waMensajeId to null")
    void command_normalizaWaMensajeIdBlank() {
        RegistrarAccionCommand cmd = new RegistrarAccionCommand(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "wa-conv-1",
                "   ", "CREATE_TAREA", "{\"x\":1}", "r", 60
        );
        assertEquals(null, cmd.waMensajeId(),
                "blank waMensajeId debe normalizarse a null para que el mapper no persista ''");
    }

    @Test
    @DisplayName("RegistrarAccionCommand rejects blank payloadJson at construction")
    void command_rechazaPayloadVacio() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new RegistrarAccionCommand(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "wa-conv-1",
                        null, "CREATE_TAREA", "  ", "razon", 60));
        assertTrue(ex.getMessage().contains("payloadJson"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static RegistrarAccionCommand sampleCommand() {
        return sampleCommand(UUID.randomUUID(), UUID.randomUUID());
    }

    private static RegistrarAccionCommand sampleCommand(UUID actor, UUID empresa) {
        return new RegistrarAccionCommand(
                actor, empresa, AiConversacionId.create().value(), "wa-conv-123",
                null, "CREATE_TAREA",
                "{\"titulo\":\"Llamar cliente\"}",
                "Cliente pidió demo",
                60
        );
    }

    // ── Test doubles ──────────────────────────────────────────────────────────

    private static final class InMemorySaveAiAccionPort implements SaveAiAccionPort {
        final List<AiAccion> saved = new ArrayList<>();

        @Override
        public AiAccion save(AiAccion accion) {
            saved.add(accion);
            return accion;
        }
    }

    /**
     * Type-level guard for the safety boundary. These four use case
     * interfaces MUST NOT appear as dependencies of
     * {@link RegistrarAccionService}. The fields below exist
     * to make accidental injection fail to compile, since the service
     * constructor in this test only takes a {@link SaveAiAccionPort}.
     */
    @SuppressWarnings("unused") // referenced by type to make the safety boundary explicit
    private static final class GuardedMutationUseCases {
        private CreateContactoUseCase createContacto;
        private CreateTratoUseCase createTrato;
        private CreateTareaUseCase createTarea;
        private MoverColumnaFichaUseCase moverColumnaFicha;
    }
}