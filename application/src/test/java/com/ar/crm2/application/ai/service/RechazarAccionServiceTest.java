package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.RechazarAccionCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.exception.AccionNotOwnedByActorException;
import com.ar.crm2.exception.AccionStateException;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RechazarAccionServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemorySaveAiAccionPort savePort;
    private InMemoryFindAiAccionPort findPort;
    private RechazarAccionService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresa));
        savePort = new InMemorySaveAiAccionPort();
        findPort = new InMemoryFindAiAccionPort();
        ActorEmpresaScopeService resolver = new ActorEmpresaScopeService(findEmpresasPort);
        service = new RechazarAccionService(
                resolver, findPort, new SaveAiAccionPortBridge(savePort)
        );
    }

    @Test
    @DisplayName("rechazar mueve PENDING → REJECTED sin tocar CRM")
    void rechazar_pending_marcaRejectedSinMutar() {
        AiAccion p = pending();
        findPort.put(p);

        AiAccion result = service.rechazar(
                new RechazarAccionCommand(actor.value(), p.getId().value(), empresa.value())
        );

        assertEquals(EstadoAccion.REJECTED, result.getEstado());
        assertEquals(EstadoAccion.REJECTED, savePort.lastSaved.getEstado());
    }

@Test
        @DisplayName("rechazar accion de otro usuario lanza AccionNotOwnedByActorException sin mutar")
        void rechazar_otroUsuario_rechaza() {
            // Both actors own the same empresa so the resolver passes; the
            // entity-level ownership check on AiAccion is what must fire.
            UsuarioId otro = UsuarioId.create();
            findEmpresasPort.ownedBy(otro, List.of(empresa));

            AiAccion p = pending();
            findPort.put(p);

            assertThrows(AccionNotOwnedByActorException.class, () -> service.rechazar(
                    new RechazarAccionCommand(otro.value(), p.getId().value(), empresa.value())
            ));
            assertEquals(0, savePort.saved.size());
        }

    @Test
    @DisplayName("rechazar accion inexistente lanza AccionNotFoundException")
    void rechazar_noExiste_lanzaNotFound() {
        assertThrows(AccionNotFoundException.class, () -> service.rechazar(
                new RechazarAccionCommand(actor.value(), UUID.randomUUID(), empresa.value())
        ));
    }

    @Test
    @DisplayName("rechazar accion ya rechazada lanza AccionStateException")
    void rechazar_yaRechazada_rechaza() {
        AiAccion p = pending().rechazar(LocalDateTime.now());
        findPort.put(p);

        assertThrows(AccionStateException.class, () -> service.rechazar(
                new RechazarAccionCommand(actor.value(), p.getId().value(), empresa.value())
        ));
    }

    @Test
    @DisplayName("rechazar con empresa seleccionada distinta al recurso lanza AsistenteTenantException (cross-check) sin mutar")
    void rechazar_empresaSeleccionadaDistintaDeRecurso_rechaza() {
        // PR6 cross-check: when the request's empresaId differs from the
        // resource's, the service rejects with a controlled
        // AsistenteTenantException BEFORE any authority / ownership
        // validation runs. Actor owns both companies to prove the
        // cross-check fires first.
        EmpresaId ajena = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(empresa, ajena));

        AiAccion p = pending(); // staged under empresa
        findPort.put(p);

        AsistenteTenantException ex = assertThrows(
                AsistenteTenantException.class,
                () -> service.rechazar(new RechazarAccionCommand(
                        actor.value(), p.getId().value(), ajena.value()  // hint != recurso
                ))
        );
        assertTrue(
                ex.getMessage().contains("no pertenece a la empresa seleccionada"),
                "expected controlled cross-check message, got: " + ex.getMessage()
        );
        assertEquals(0, savePort.saved.size(), "rejected before any save");
    }

    @Test
    @DisplayName("rechazar cuando actor no posee la empresa del recurso lanza AsistenteTenantException (ownership)")
    void rechazar_actorNoPoseeTenantDelRecurso_rechaza() {
        // PR6 ownership triangulation: hint matches the resource but the
        // actor does NOT own that tenant — the Empresa-owned port's
        // TenantScopeViolationException is translated into
        // AsistenteTenantException at the AI call site.
        EmpresaId ajena = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(ajena));  // owns ajena, NOT empresa

        AiAccion p = pending(); // staged under empresa (which actor doesn't own)
        findPort.put(p);

        assertThrows(
                AsistenteTenantException.class,
                () -> service.rechazar(new RechazarAccionCommand(
                        actor.value(), p.getId().value(), empresa.value()  // hint == recurso
                ))
        );
        assertEquals(0, savePort.saved.size());
    }

    private AiAccion pending() {
        return AiAccion.crear(
                empresa, actor, "wa-conv", null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_TAREA",
                "{\"titulo\":\"x\"}", "r", 60, LocalDateTime.now()
        );
    }

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();
        /** Replaces the existing row for {@code u} (idempotent). */
        void ownedBy(UsuarioId u, List<EmpresaId> e) {
            rows.removeIf(r -> r.user.equals(u));
            rows.add(new Row(u, e));
        }
        @Override public List<EmpresaId> findEmpresasByCreador(UsuarioId u) {
            return rows.stream().filter(r -> r.user.equals(u)).findFirst()
                    .map(r -> r.empresas).orElse(List.of());
        }
        private record Row(UsuarioId user, List<EmpresaId> empresas) {}
    }

    private static final class InMemorySaveAiAccionPort
            implements com.ar.crm2.application.ai.port.out.SaveAiAccionPort {
        final List<AiAccion> saved = new ArrayList<>();
        AiAccion lastSaved;
        @Override public AiAccion save(AiAccion p) { saved.add(p); lastSaved = p; return p; }
    }

    private static final class InMemoryFindAiAccionPort implements FindAiAccionPort {
        private final List<AiAccion> rows = new ArrayList<>();
        void put(AiAccion p) { rows.add(p); }
        @Override public Optional<AiAccion> findById(UUID id) {
            return rows.stream().filter(p -> p.getId().value().equals(id)).findFirst();
        }
    }
}
