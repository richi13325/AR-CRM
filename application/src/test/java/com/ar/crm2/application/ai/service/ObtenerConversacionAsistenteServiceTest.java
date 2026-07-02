package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.command.ObtenerConversacionAsistenteCommand;
import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.ai.exception.ConversacionAsistenteNoEncontradaException;
import com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase.ResultadoConversacionAsistente;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.application.empresa.service.ActorEmpresaScopeService;
// ActorEmpresaScopeService implements ActorEmpresaScopePort directly, so the
// test injects the Empresa-owned service as the AI dependency.
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;
import com.ar.crm2.model.enums.RolMensajeAi;
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

/**
 * Tests for {@link ObtenerConversacionAsistenteService}.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Not-found when id does not exist.</li>
 *   <li>Tenant mismatch (actor or empresa) rejected without leaking messages.</li>
 *   <li>Happy path returns conversation + ordered messages.</li>
 * </ul>
 */
class ObtenerConversacionAsistenteServiceTest {

    private final UsuarioId actor = UsuarioId.create();
    private final UsuarioId otro = UsuarioId.create();
    private final EmpresaId empresa = EmpresaId.create();

    private InMemoryFindEmpresasByCreadorPort findEmpresasPort;
    private InMemoryFindAiConversacionPort findConv;
    private InMemoryFindAiMensajes findMensajes;
    private ObtenerConversacionAsistenteService service;

    @BeforeEach
    void setUp() {
        findEmpresasPort = new InMemoryFindEmpresasByCreadorPort();
        findEmpresasPort.ownedBy(actor, List.of(empresa));
        findConv = new InMemoryFindAiConversacionPort();
        findMensajes = new InMemoryFindAiMensajes();
service = new ObtenerConversacionAsistenteService(
                new ActorEmpresaScopeService(findEmpresasPort), findConv, findMensajes
        );
    }

    @Test
    @DisplayName("obtener conversación inexistente lanza not found")
    void obtener_noExiste_lanzaNotFound() {
        assertThrows(ConversacionAsistenteNoEncontradaException.class,
                () -> service.obtener(new ObtenerConversacionAsistenteCommand(
                        actor.value(), UUID.randomUUID(), empresa.value())));
    }

@Test
        @DisplayName("obtener conversación de otro actor lanza ConversacionAsistenteNotOwnedByActorException sin retornar mensajes")
        void obtener_otroActor_lanzaExcepcionSinMensajes() {
            // Both actors own the same empresa so the resolver passes; the
            // entity-level ownership check on AiConversacion is what must fire.
            findEmpresasPort.ownedBy(otro, List.of(empresa));

            AiConversacion c = AiConversacion.crear(empresa, otro, "wa-conv", null, LocalDateTime.now());
            findConv.put(c);

            assertThrows(ConversacionAsistenteNotOwnedByActorException.class,
                    () -> service.obtener(new ObtenerConversacionAsistenteCommand(
                            actor.value(), c.getId().value(), empresa.value())));
        }

    @Test
    @DisplayName("obtener conversación propia devuelve conversación + mensajes")
    void obtener_propia_devuelveConversacionYMensajes() {
        AiConversacion c = AiConversacion.crear(empresa, actor, "wa-conv", null, LocalDateTime.now());
        findConv.put(c);
        findMensajes.mensajes.add(AiMensaje.crear(
                c.getId(), RolMensajeAi.USER, "hola", null,
                null, null, null, null, LocalDateTime.now()
        ));

        ResultadoConversacionAsistente result = service.obtener(
                new ObtenerConversacionAsistenteCommand(actor.value(), c.getId().value(), empresa.value())
        );

        assertEquals(c.getId(), result.conversacion().getId());
        assertEquals(1, result.mensajes().size());
    }

    @Test
    @DisplayName("obtener con empresa seleccionada distinta al recurso lanza AsistenteTenantException (cross-check)")
    void obtener_empresaSeleccionadaDistintaDeRecurso_rechaza() {
        // PR6 cross-check: when the request's empresaId differs from the
        // resource's, the service rejects with a controlled
        // AsistenteTenantException BEFORE any authority / ownership
        // validation runs. Actor owns both companies to prove the
        // cross-check fires first.
        EmpresaId ajena = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(empresa, ajena));

        AiConversacion c = AiConversacion.crear(empresa, actor, "wa-conv", null, LocalDateTime.now());
        findConv.put(c);

        AsistenteTenantException ex = assertThrows(
                AsistenteTenantException.class,
                () -> service.obtener(new ObtenerConversacionAsistenteCommand(
                        actor.value(), c.getId().value(), ajena.value()  // hint != recurso
                ))
        );
        assertTrue(
                ex.getMessage().contains("no pertenece a la empresa seleccionada"),
                "expected controlled cross-check message, got: " + ex.getMessage()
        );
    }

    @Test
    @DisplayName("obtener cuando actor no posee la empresa del recurso lanza AsistenteTenantException (ownership)")
    void obtener_actorNoPoseeTenantDelRecurso_rechaza() {
        // PR6 ownership triangulation: hint matches the resource but the
        // actor does NOT own that tenant — the Empresa-owned port's
        // TenantScopeViolationException is translated into
        // AsistenteTenantException at the AI call site.
        EmpresaId ajena = EmpresaId.create();
        findEmpresasPort.ownedBy(actor, List.of(ajena));  // owns ajena, NOT empresa

        AiConversacion c = AiConversacion.crear(empresa, actor, "wa-conv", null, LocalDateTime.now());
        findConv.put(c);

        assertThrows(
                AsistenteTenantException.class,
                () -> service.obtener(new ObtenerConversacionAsistenteCommand(
                        actor.value(), c.getId().value(), empresa.value()  // hint == recurso
                ))
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

    private static final class InMemoryFindAiConversacionPort implements FindAiConversacionPort {
        private final List<AiConversacion> rows = new ArrayList<>();
        void put(AiConversacion c) { rows.add(c); }
        @Override public Optional<AiConversacion> findById(UUID id) {
            return rows.stream().filter(c -> c.getId().value().equals(id)).findFirst();
        }
    }

    private static final class InMemoryFindAiMensajes implements FindAiMensajesByConversacionPort {
        final List<AiMensaje> mensajes = new ArrayList<>();
        @Override public List<AiMensaje> findByConversacionId(UUID id) { return List.copyOf(mensajes); }
    }
}
