package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ListarAccionesPendientesCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.port.out.ListPendingAiAccionesPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ListarAccionesPendientesService}.
 *
 * <p><b>User-approved PR7 semantics (strict):</b>
 * <ul>
 *   <li>{@code empresaId} MUST be supplied on the command. The service
 *       MUST NOT auto-resolve single-company actors; it MUST NOT guess
 *       or fall back to the first owned company.</li>
 *   <li>When the supplied {@code empresaId} is not owned by the actor
 *       (or the actor owns no companies), the service MUST throw
 *       {@link AsistenteTenantException} via the
 *       {@link AiTenantExceptionTranslator}.</li>
 *   <li>When the actor owns the supplied {@code empresaId}, the
 *       service delegates to {@link ListPendingAiAccionesPort} with
 *       the resolved empresaId and the supplied limit. PENDING-only
 *       filtering is the port's responsibility.</li>
 * </ul>
 */
class ListarAccionesPendientesServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();
    private final EmpresaId ajena = EmpresaId.create();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemoryListPendingAiAccionesPort listPort;
    private ListarAccionesPendientesService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresa));
        listPort = new InMemoryListPendingAiAccionesPort();
        service = new ListarAccionesPendientesService(
                new ActorEmpresaScopeService(findEmpresasPort),
                listPort
        );
    }

    @Test
    @DisplayName("listar delega al port con actor + empresaId + limite cuando el actor posee la empresa")
    void listar_delegateAlPortCuandoActorPoseeEmpresa() {
        AiAccion pending = buildAccion(empresa, actor, EstadoAccion.PENDING);
        listPort.rows.add(pending);

        List<AiAccion> result = service.listar(
                new ListarAccionesPendientesCommand(actor.value(), empresa.value(), 25)
        );

        assertEquals(1, result.size());
        assertEquals(pending.getId(), result.get(0).getId());
        assertEquals(actor.value(), listPort.lastActor);
        assertEquals(empresa.value(), listPort.lastEmpresa);
        assertEquals(25, listPort.lastLimite);
    }

    @Test
    @DisplayName("listar retorna lista vacia cuando el actor no tiene acciones PENDING")
    void listar_listaVaciaCuandoNoHayAcciones() {
        List<AiAccion> result = service.listar(
                new ListarAccionesPendientesCommand(actor.value(), empresa.value(), 50)
        );

        assertEquals(0, result.size());
        assertEquals(empresa.value(), listPort.lastEmpresa);
    }

    @Test
    @DisplayName("listar NO auto-resuelve empresaId cuando el command trae null — falla por invariante")
    void listar_empresaIdNull_esRechazadoPorCommand() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ListarAccionesPendientesCommand(actor.value(), null, 50));

        assertEquals("empresaId is required", ex.getMessage());
    }

    @Test
    @DisplayName("listar lanza AsistenteTenantException cuando el actor no posee la empresa solicitada")
    void listar_actorNoPoseeEmpresa_lanzaAsistenteTenantException() {
        // The actor owns only `empresa` but requests `ajena`. The service
        // MUST NOT silently return an empty list — it MUST throw the
        // controlled AI-public exception that the GlobalExceptionHandler
        // maps to a 403.
        AsistenteTenantException ex = assertThrows(AsistenteTenantException.class,
                () -> service.listar(
                        new ListarAccionesPendientesCommand(actor.value(), ajena.value(), 50)
                ));

        assertTrue(
                ex.getMessage().contains(actor.value().toString())
                        || ex.getMessage().contains("empresa"),
                "exception message must reference the actor or the empresa; got: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("listar lanza AsistenteTenantException cuando el actor no posee ninguna empresa")
    void listar_actorSinEmpresas_lanzaAsistenteTenantException() {
        UsuarioId otroActor = UsuarioId.create();
        // `otroActor` owns nothing.
        AsistenteTenantException ex = assertThrows(AsistenteTenantException.class,
                () -> service.listar(
                        new ListarAccionesPendientesCommand(otroActor.value(), empresa.value(), 50)
                ));

        assertTrue(
                ex.getMessage().contains(otroActor.value().toString())
                        || ex.getMessage().toLowerCase().contains("empresa"),
                "exception message must reference the actor or empresa; got: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("listar multi-empresa: actor con varias empresas NO auto-resuelve — empresa es REQUIRED en el command")
    void listar_multiEmpresa_empresaEsRequeridaNoAutoresuelve() {
        EmpresaId otra = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(empresa, otra));

        // With more than one owned company, the user-approved PR7 contract
        // says the caller MUST supply empresaId. The command constructor
        // rejects null with IllegalArgumentException.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ListarAccionesPendientesCommand(actor.value(), null, 50));

        assertEquals("empresaId is required", ex.getMessage());
    }

    private AiAccion buildAccion(EmpresaId empresaId, UsuarioId actorId, EstadoAccion estado) {
        return AiAccion.reconstitute(
                AiAccionId.create(),
                empresaId,
                actorId,
                "wa-conv-1",
                null,
                AiConversacionId.from(UUID.randomUUID()),
                TipoAccion.CREATE_CONTACTO.name(),
                "{}",
                "r",
                1,
                LocalDateTime.now().plusHours(1),
                null,
                null,
                estado,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();
        void ownedBy(UsuarioId u, List<EmpresaId> e) { rows.add(new Row(u, e)); }
        @Override public List<EmpresaId> findEmpresasByCreador(UsuarioId u) {
            return rows.stream().filter(r -> r.user.equals(u)).findFirst()
                    .map(r -> r.empresas).orElse(List.of());
        }
        private record Row(UsuarioId user, List<EmpresaId> empresas) {}
    }

    private static final class InMemoryListPendingAiAccionesPort implements ListPendingAiAccionesPort {
        final List<AiAccion> rows = new ArrayList<>();
        UUID lastActor;
        UUID lastEmpresa;
        int lastLimite;
        @Override public List<AiAccion> listPendingByActor(UUID actor, UUID empresa, int limite) {
            lastActor = actor;
            lastEmpresa = empresa;
            lastLimite = limite;
            return new ArrayList<>(rows);
        }
    }
}
