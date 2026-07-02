package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ObtenerAccionCommand;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.exception.AccionNotFoundException;
import com.ar.crm2.exception.AccionNotOwnedByActorException;
import com.ar.crm2.model.entity.ia.AiAccion;
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

class ObtenerAccionServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final UsuarioId otro = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemoryFindAiAccionPort findPort;
    private ObtenerAccionService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresa));
        findPort = new InMemoryFindAiAccionPort();
        ActorEmpresaScopeService resolver = new ActorEmpresaScopeService(findEmpresasPort);
        service = new ObtenerAccionService(resolver, findPort);
    }

    @Test
    @DisplayName("obtener accion inexistente lanza AccionNotFoundException")
    void obtener_noExiste_lanzaNotFound() {
        assertThrows(AccionNotFoundException.class,
                () -> service.obtener(new ObtenerAccionCommand(
                        actor.value(), UUID.randomUUID(), empresa.value())));
    }

@Test
        @DisplayName("obtener accion de otro actor lanza AccionNotOwnedByActorException")
        void obtener_otroActor_lanzaExcepcion() {
            // Both actors own the same empresa so the resolver passes; the
            // entity-level ownership check on AiAccion is what must fire
            // because the proposal was staged under 'otro', not 'actor'.
            findEmpresasPort.ownedBy(otro, List.of(empresa));

            AiAccion p = AiAccion.crear(
                    empresa, otro, "wa", null,
                    com.ar.crm2.model.vo.AiConversacionId.create(),
                    "CREATE_TAREA", "{\"titulo\":\"x\"}",
                    "r", 60, LocalDateTime.now()
            );
            findPort.put(p);

            assertThrows(AccionNotOwnedByActorException.class,
                    () -> service.obtener(new ObtenerAccionCommand(
                            actor.value(), p.getId().value(), empresa.value())));
        }

    @Test
    @DisplayName("obtener accion propia la devuelve")
    void obtener_propia_devuelveAccion() {
        AiAccion p = AiAccion.crear(
                empresa, actor, "wa", null,
                com.ar.crm2.model.vo.AiConversacionId.create(),
                "CREATE_TAREA", "{\"titulo\":\"x\"}",
                "r", 60, LocalDateTime.now()
        );
        findPort.put(p);

        AiAccion result = service.obtener(new ObtenerAccionCommand(
                actor.value(), p.getId().value(), empresa.value()));

        assertEquals(p.getId(), result.getId());
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

    private static final class InMemoryFindAiAccionPort implements FindAiAccionPort {
        private final List<AiAccion> rows = new ArrayList<>();
        void put(AiAccion p) { rows.add(p); }
        @Override public Optional<AiAccion> findById(UUID id) {
            return rows.stream().filter(p -> p.getId().value().equals(id)).findFirst();
        }
    }
}
