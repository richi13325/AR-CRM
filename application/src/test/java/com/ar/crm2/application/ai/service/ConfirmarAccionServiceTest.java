package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ConfirmarAccionCommand;
import com.ar.crm2.application.ai.exception.AccionInvalidaException;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.in.result.ResultadoEjecucionAccion;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.contacto.command.CreateContactoCommand;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.application.ficha.command.MoverColumnaFichaCommand;
import com.ar.crm2.application.ficha.port.in.MoverColumnaFichaUseCase;
import com.ar.crm2.application.tarea.command.CreateTareaCommand;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.trato.command.CreateTratoCommand;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.exception.AccionExpiredException;
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.exception.AccionNotOwnedByActorException;
import com.ar.crm2.exception.AccionStateException;
import com.ar.crm2.exception.AccionVersionMismatchException;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.entity.Trato;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.enums.EstadoRelacion;
import com.ar.crm2.model.enums.PrioridadTarea;
import com.ar.crm2.model.enums.TipoContrato;
import com.ar.crm2.model.enums.TipoTarea;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Safety-boundary tests for {@link ConfirmarAccionService}.
 *
 * <p>This is the SINGLE application service allowed to invoke real
 * CRM mutation use cases. The tests below assert the non-negotiable
 * invariants: owner-only, tenant-only, PENDING-only, version match,
 * not expired, and EXECUTED/FAILED lifecycle bookkeeping.
 */
class ConfirmarAccionServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final UsuarioId otherActor = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();
    private final String waConv = "wa-conv-1";

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemorySaveAiAccionPort savePort;
    private InMemoryFindAiAccionPort findPort;
    private SpyCreateContactoUseCase createContacto;
    private SpyCreateTratoUseCase createTrato;
    private SpyCreateTareaUseCase createTarea;
    private SpyMoverColumnaFichaUseCase moverFicha;

private ActorEmpresaScopeService resolver;
    private ConfirmarAccionService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresa));
        resolver = new ActorEmpresaScopeService(findEmpresasPort);

        savePort = new InMemorySaveAiAccionPort();
        findPort = new InMemoryFindAiAccionPort();

        createContacto = new SpyCreateContactoUseCase();
        createTrato = new SpyCreateTratoUseCase();
        createTarea = new SpyCreateTareaUseCase();
        moverFicha = new SpyMoverColumnaFichaUseCase();

        service = new ConfirmarAccionService(
                resolver, findPort, new SaveAiAccionPortBridge(savePort),
                createContacto, createTrato, createTarea, moverFicha
        );
    }

    @Nested
    @DisplayName("Ownership + tenant checks")
    class Ownership {

        @Test
        @DisplayName("confirmar de accion ajena lanza AccionNotOwnedByActorException sin mutar CRM")
        void confirmar_deOtroUsuario_rechazaSinMutar() {
            // Both actors own the same empresa so the resolver passes; the
            // entity-level ownership check on AiAccion is what must fire.
            findEmpresasPort.ownedBy(otherActor, List.of(empresa));
            AiAccion p = pending();
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    otherActor.value(), p.getId().value(), 1, empresa.value()
            );

            assertThrows(AccionNotOwnedByActorException.class, () -> service.confirmar(cmd));
            assertEquals(0, createContacto.calls);
            assertEquals(0, savePort.saved.size()); // rejected before any save
        }

        @Test
        @DisplayName("confirmar con empresa seleccionada distinta al recurso lanza AsistenteTenantException (cross-check) sin mutar")
        void confirmar_empresaNoPoseida_rechaza() {
            // PR6 cross-check (PR5 corrective + PR6 resource-first):
            // when the request's empresaId differs from the resource's,
            // the service rejects with a controlled AsistenteTenantException
            // BEFORE any authority / ownership validation runs. The actor
            // owns both companies on purpose so we can prove the cross-check
            // fires first (not the Empresa-owned ownership validator).
            EmpresaId ajena = EmpresaId.create();
            findEmpresasPort.ownedBy(actor, List.of(empresa, ajena));

            AiAccion p = pending(); // staged under empresa
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, ajena.value()  // hint != recurso
            );

            AsistenteTenantException ex = assertThrows(
                    AsistenteTenantException.class,
                    () -> service.confirmar(cmd)
            );
            // Controlled message: "This action does not belong to the selected company".
            assertTrue(
                    ex.getMessage().contains("no pertenece a la empresa seleccionada"),
                    "expected controlled cross-check message, got: " + ex.getMessage()
            );
            assertEquals(0, createContacto.calls);
            assertEquals(0, savePort.saved.size(), "rejected before any save");
        }

        @Test
        @DisplayName("confirmar cuando actor no posee la empresa del recurso lanza AsistenteTenantException (ownership)")
        void confirmar_actorNoPoseeTenantDelRecurso_rechaza() {
            // PR6 ownership branch (triangulation): the request's empresaId
            // matches the resource, BUT the actor does NOT own that tenant.
            // The Empresa-owned port rejects with TenantScopeViolationException,
            // which the AI call site translates into AsistenteTenantException.
            EmpresaId ajena = EmpresaId.create();
            findEmpresasPort.ownedBy(actor, List.of(ajena));  // owns ajena, NOT empresa

            AiAccion p = pending(); // staged under empresa (which actor doesn't own)
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()  // hint == recurso
            );

            assertThrows(AsistenteTenantException.class, () -> service.confirmar(cmd));
            assertEquals(0, createContacto.calls);
        }
    }

    @Nested
    @DisplayName("State machine")
    class StateMachine {

        @Test
        @DisplayName("confirmar de accion no PENDING lanza AccionStateException")
        void confirmar_noPending_rechaza() {
            AiAccion p = pending();
            AiAccion rechazada = p.rechazar(LocalDateTime.now());
            findPort.put(rechazada);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            assertThrows(AccionStateException.class, () -> service.confirmar(cmd));
            assertEquals(0, createContacto.calls);
        }

        @Test
        @DisplayName("accion no encontrada lanza AccionNotFoundException")
        void confirmar_noExiste_rechaza() {
            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), UUID.randomUUID(), 1, empresa.value()
            );
            assertThrows(AccionNotFoundException.class, () -> service.confirmar(cmd));
            assertEquals(0, createContacto.calls);
        }
    }

    @Nested
    @DisplayName("Version + expiry")
    class VersionAndExpiry {

        @Test
        @DisplayName("expectedVersion que no coincide lanza AccionVersionMismatchException")
        void confirmar_versionInconsistente_rechaza() {
            AiAccion p = pending();
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 99, empresa.value()
            );

            assertThrows(AccionVersionMismatchException.class, () -> service.confirmar(cmd));
            assertEquals(0, createContacto.calls);
        }

    @Test
    @DisplayName("accion ya expirada al confirmar lanza AccionExpiredException")
    void confirmar_accionExpirada_lanzaExcepcionYMarcExpired() {
        // Build a proposal already past its expiry by reconstituting one with past expiresAt.
        AiAccion p = AiAccion.reconstitute(
                AiAccionId.create(), empresa, actor, waConv,
                null, com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_CONTACTO",
                "{\"empresaId\":\"" + empresa.value() + "\",\"nombre\":\"Juan\","
                        + "\"estadoRelacion\":\"PROSPECTO\","
                        + "\"responsableId\":\"" + actor.value() + "\"}",
                "r", 1, LocalDateTime.now().minusHours(1),  // expiresAt in the past
                null, null, EstadoAccion.PENDING,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1)
        );
        findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            assertThrows(AccionExpiredException.class, () -> service.confirmar(cmd));
            assertEquals(1, savePort.saved.size(), "debe persistir la accion marcada EXPIRED");
            assertEquals(EstadoAccion.EXPIRED, savePort.lastSaved.getEstado());
        }
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("confirmar CREATE_CONTACTO dispatcha CreateContactoUseCase y marca EXECUTED")
        void confirmar_createContacto_dispatchaYMarcaExecuted() {
            AiAccion p = pending();
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            ResultadoEjecucionAccion result = service.confirmar(cmd);

            assertEquals(EstadoAccion.EXECUTED, result.estado());
            assertNotNull(result.resultadoEntidadId());
            assertEquals(1, createContacto.calls);
            assertEquals(EstadoAccion.EXECUTED, savePort.lastSaved.getEstado());
        }

        @Test
        @DisplayName("confirmar CREATE_TRATO dispatcha CreateTratoUseCase")
        void confirmar_createTrato_dispatcha() {
            AiAccion p = pending("CREATE_TRATO", CreateTratoPayload.build(actor, empresa));
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            service.confirmar(cmd);
            assertEquals(1, createTrato.calls);
        }

        @Test
        @DisplayName("confirmar CREATE_TAREA dispatcha CreateTareaUseCase")
        void confirmar_createTarea_dispatcha() {
            AiAccion p = pending("CREATE_TAREA", CreateTareaPayload.build(actor, empresa));
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            service.confirmar(cmd);
            assertEquals(1, createTarea.calls);
        }

        @Test
        @DisplayName("confirmar MOVE_KANBAN_FICHA dispatcha MoverColumnaFichaUseCase")
        void confirmar_moveFicha_dispatcha() {
            AiAccion p = pending("MOVE_KANBAN_FICHA", MoveFichaPayload.build(actor, empresa));
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            service.confirmar(cmd);
            assertEquals(1, moverFicha.calls);
        }
    }

    @Nested
    @DisplayName("Mutation failure")
    class MutationFailure {

        @Test
        @DisplayName("payload JSON invalido se propaga como AccionInvalidaException y NO marca FAILED")
        void confirmar_payloadJsonInvalido_propagaExcepcionControlada() {
            AiAccion p = pending("CREATE_CONTACTO", "{malformed-json");
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            AccionInvalidaException ex = assertThrows(
                    AccionInvalidaException.class,
                    () -> service.confirmar(cmd)
            );

            assertTrue(ex.getMessage().contains("JSON inválido"));
            assertEquals(0, createContacto.calls);
            assertEquals(0, savePort.saved.size(), "invalid payload must not be persisted as FAILED");
        }

        @Test
        @DisplayName("payload con campo requerido null se propaga como AccionInvalidaException y NO marca FAILED")
        void confirmar_payloadConCampoRequeridoNull_propagaExcepcionControlada() {
            String payload = "{\"tratoId\":\"" + UUID.randomUUID() + "\","
                    + "\"responsableId\":\"" + actor.value() + "\","
                    + "\"titulo\":\"Llamar\","
                    + "\"descripcion\":null,"
                    + "\"tipo\":\"GENERAL\","
                    + "\"prioridad\":\"ALTA\"}";
            AiAccion p = pending("CREATE_TAREA", payload);
            findPort.put(p);

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            AccionInvalidaException ex = assertThrows(
                    AccionInvalidaException.class,
                    () -> service.confirmar(cmd)
            );

            assertTrue(ex.getMessage().contains("campo obligatorio: descripcion"));
            assertEquals(0, createTarea.calls);
            assertEquals(0, savePort.saved.size(), "invalid payload must not be persisted as FAILED");
        }

        @Test
        @DisplayName("si CreateContactoUseCase lanza, accion queda FAILED con errorReason")
        void confirmar_dispatchFalla_marcaFailedConReason() {
            AiAccion p = pending();
            findPort.put(p);

            createContacto.failWith = new IllegalStateException("Contacto duplicado");

            ConfirmarAccionCommand cmd = new ConfirmarAccionCommand(
                    actor.value(), p.getId().value(), 1, empresa.value()
            );

            ResultadoEjecucionAccion result = service.confirmar(cmd);

            assertEquals(EstadoAccion.FAILED, result.estado());
            assertEquals("Contacto duplicado", result.errorReason());
            assertEquals(EstadoAccion.FAILED, savePort.lastSaved.getEstado());
            assertNotNull(savePort.lastSaved.getErrorReason());
        }
    }

    // ── helpers ──────────────────────────────────────────────

    private AiAccion pending(String tipoAccion, String payload) {
        return AiAccion.crear(
                empresa, actor, waConv, null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                tipoAccion, payload, "r", 60, LocalDateTime.now()
        );
    }

    private AiAccion pending() {
        return pending("CREATE_CONTACTO",
                "{\"empresaId\":\"" + empresa.value() + "\","
                        + "\"nombre\":\"Juan\","
                        + "\"estadoRelacion\":\"PROSPECTO\","
                        + "\"responsableId\":\"" + actor.value() + "\"}");
    }

    private static final class CreateContactoPayload {
        static String build(UsuarioId actor, EmpresaId empresa) {
            return "{\"empresaId\":\"" + empresa.value() + "\","
                    + "\"nombre\":\"Juan\","
                    + "\"estadoRelacion\":\"PROSPECTO\","
                    + "\"responsableId\":\"" + actor.value() + "\"}";
        }
    }

    private static final class CreateTratoPayload {
        static String build(UsuarioId actor, EmpresaId empresa) {
            return "{\"contactoId\":\"" + UUID.randomUUID() + "\","
                    + "\"responsableId\":\"" + actor.value() + "\","
                    + "\"nombre\":\"Venta\","
                    + "\"tipoContrato\":\"LICENCIA\"}";
        }
    }

    private static final class CreateTareaPayload {
        static String build(UsuarioId actor, EmpresaId empresa) {
            return "{\"tratoId\":\"" + UUID.randomUUID() + "\","
                    + "\"responsableId\":\"" + actor.value() + "\","
                    + "\"titulo\":\"Llamar\","
                    + "\"descripcion\":\"Llamar al cliente ma\u00f1ana\","
                    + "\"tipo\":\"GENERAL\","
                    + "\"prioridad\":\"ALTA\"}";
        }
    }

    private static final class MoveFichaPayload {
        static String build(UsuarioId actor, EmpresaId empresa) {
            return "{\"fichaId\":\"" + UUID.randomUUID() + "\","
                    + "\"targetColumnaId\":\"" + UUID.randomUUID() + "\"}";
        }
    }

    // ── In-memory ports ──────────────────────────────────────────

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();

        /** Replaces the existing row for {@code user} (idempotent). */
        void ownedBy(UsuarioId user, List<EmpresaId> empresas) {
            rows.removeIf(r -> r.user.equals(user));
            rows.add(new Row(user, new ArrayList<>(empresas)));
        }

        @Override
        public List<EmpresaId> findEmpresasByCreador(UsuarioId creadoPor) {
            return rows.stream()
                    .filter(r -> r.user.equals(creadoPor))
                    .findFirst()
                    .map(r -> List.copyOf(r.empresas))
                    .orElse(List.of());
        }

        private record Row(UsuarioId user, List<EmpresaId> empresas) {}
    }

    private static final class InMemorySaveAiAccionPort
            implements com.ar.crm2.application.ai.port.out.SaveAiAccionPort {
        final List<AiAccion> saved = new ArrayList<>();
        AiAccion lastSaved;

        @Override
        public AiAccion save(AiAccion accion) {
            saved.add(accion);
            lastSaved = accion;
            return accion;
        }
    }

    private static final class InMemoryFindAiAccionPort implements FindAiAccionPort {
        private final List<AiAccion> rows = new ArrayList<>();

        void put(AiAccion p) {
            rows.add(p);
        }

        @Override
        public java.util.Optional<AiAccion> findById(UUID id) {
            return rows.stream().filter(p -> p.getId().value().equals(id)).findFirst();
        }
    }

    private static final class SpyCreateContactoUseCase implements CreateContactoUseCase {
        int calls;
        RuntimeException failWith;

        @Override
        public Contacto create(CreateContactoCommand command) {
            calls++;
            if (failWith != null) throw failWith;
            // Return a real Contacto for assertions.
            return Contacto.create(
                    EmpresaId.from(command.empresaId()),
                    command.nombre(),
                    command.correo(),
                    command.estadoRelacion() == null ? EstadoRelacion.PROSPECTO : command.estadoRelacion(),
                    command.responsableId() == null ? null : UsuarioId.from(command.responsableId()),
                    command.creadoPor() == null ? null : UsuarioId.from(command.creadoPor()),
                    command.telefono(),
                    command.cargo(),
                    command.comoNosConocio()
            );
        }
    }

    private static final class SpyCreateTratoUseCase implements CreateTratoUseCase {
        int calls;

        @Override
        public Trato create(CreateTratoCommand command) {
            calls++;
            return Trato.create(
                    ContactoId.from(command.contactoId()),
                    UsuarioId.from(command.responsableId()),
                    command.nombre(),
                    command.valorEstimado(),
                    command.probabilidad(),
                    command.fechaCierreEsperada(),
                    command.tipoContrato() == null ? TipoContrato.LICENCIA : command.tipoContrato()
            );
        }
    }

    private static final class SpyCreateTareaUseCase implements CreateTareaUseCase {
        int calls;

        @Override
        public Tarea create(CreateTareaCommand command) {
            calls++;
            return Tarea.create(
                    TratoId.from(command.tratoId()),
                    UsuarioId.from(command.responsableId()),
                    command.titulo(),
                    command.descripcion(),
                    command.tipo() == null ? TipoTarea.GENERAL : command.tipo(),
                    command.prioridad() == null ? PrioridadTarea.ALTA : command.prioridad(),
                    command.fechaLimite()
            );
        }
    }

    private static final class SpyMoverColumnaFichaUseCase implements MoverColumnaFichaUseCase {
        int calls;

        @Override
        public Ficha moverAColumna(MoverColumnaFichaCommand command) {
            calls++;
            // Return a real Ficha (empty empresa scope is fine for assertions).
            return Ficha.create(
                    ColumnaId.from(command.targetColumnaId()),
                    com.ar.crm2.model.enums.TipoFicha.TAREA,
                    null, null
            );
        }
    }
}
