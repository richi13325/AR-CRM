package com.ar.crm2.application.empresa.service;

import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.exception.TenantScopeViolationException;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ActorEmpresaScopeService}.
 *
 * <p>This service is intentionally a neutral reusable Empresa
 * application service: it does NOT depend on any
 * {@code application.ai.*} type. Tenant scope resolution is a
 * general Empresa concern; the AI assistant is one consumer but not
 * the only intended one. The neutral domain exception
 * {@link TenantScopeViolationException} propagates as-is; callers
 * translate it at their own boundary.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Single-empresa resolver: when no explicit empresaId is provided,
 *       the FIRST owned company is returned.</li>
 *   <li>Explicit empresaId: must be in the owned set, otherwise rejected
 *       with the neutral {@link TenantScopeViolationException}.</li>
 *   <li>Actor owns no companies → tenant exception.</li>
 *   <li>Null actorUsuarioId → {@link IllegalArgumentException}.</li>
 *   <li>{@code empresasDelActor(...)} returns the full owned list for
 *       cross-checks (e.g. multi-empresa future support).</li>
 * </ul>
 */
class ActorEmpresaScopeServiceTest {

    private InMemoryFindEmpresasByCreadorPort port;
    private ActorEmpresaScopeService service;

    private final UsuarioId actor = UsuarioId.create();
    private final EmpresaId empresa1 = EmpresaId.create();
    private final EmpresaId empresa2 = EmpresaId.create();

    @BeforeEach
    void setUp() {
        port = new InMemoryFindEmpresasByCreadorPort();
        port.ownedBy(actor, List.of(empresa1, empresa2));
        service = new ActorEmpresaScopeService(port);
    }

    @Test
    @DisplayName("resolver sin empresaId devuelve la primera empresa del actor")
    void resolver_sinEmpresaId_devuelvePrimera() {
        EmpresaId result = service.resolver(actor.value(), null);
        assertEquals(empresa1, result);
    }

    @Test
    @DisplayName("resolver con empresaId válida la devuelve")
    void resolver_conEmpresaIdValida_devuelveEsaMisma() {
        EmpresaId result = service.resolver(actor.value(), empresa2.value());
        assertEquals(empresa2, result);
    }

    @Test
    @DisplayName("resolver con empresaId no poseída rechaza con TenantScopeViolationException neutral")
    void resolver_conEmpresaIdNoPoseida_lanzaExcepcion() {
        UUID ajena = UUID.randomUUID();
        TenantScopeViolationException ex = assertThrows(TenantScopeViolationException.class,
                () -> service.resolver(actor.value(), ajena));
        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank(),
                "el mensaje neutral debe estar presente");
    }

    @Test
    @DisplayName("resolver con actor que no posee ninguna empresa rechaza")
    void resolver_actorSinEmpresas_lanzaExcepcion() {
        UsuarioId otroActor = UsuarioId.create();
        port.ownedBy(otroActor, List.of());
        assertThrows(TenantScopeViolationException.class,
                () -> service.resolver(otroActor.value(), null));
    }

    @Test
    @DisplayName("resolver con actorUsuarioId null lanza IllegalArgumentException")
    void resolver_actorNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.resolver(null, null));
    }

    @Test
    @DisplayName("empresasDelActor devuelve la lista completa de empresas poseídas")
    void empresasDelActor_devuelveListaCompleta() {
        List<EmpresaId> result = service.empresasDelActor(actor.value());
        assertEquals(2, result.size());
        assertTrue(result.contains(empresa1));
        assertTrue(result.contains(empresa2));
    }

    @Test
    @DisplayName("empresasDelActor devuelve lista vacía para actor sin empresas")
    void empresasDelActor_actorSinEmpresas_devuelveVacia() {
        UsuarioId otroActor = UsuarioId.create();
        port.ownedBy(otroActor, List.of());
        List<EmpresaId> result = service.empresasDelActor(otroActor.value());
        assertTrue(result.isEmpty());
    }

    // ── In-memory port ──────────────────────────────────────────

    private static final class InMemoryFindEmpresasByCreadorPort implements FindEmpresasByCreadorPort {
        private final List<Row> rows = new ArrayList<>();

        void ownedBy(UsuarioId user, List<EmpresaId> empresas) {
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
}